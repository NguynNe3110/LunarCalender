package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.LunarCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    // Calendar state, which is automatically updated onResume to prevent displaying old cached date
    private val calendarState = mutableStateOf(Calendar.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LunarCalendarBentoScreen(
                        calendar = calendarState.value,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Force update calendar state to current time when app is opened or brought to foreground
        calendarState.value = Calendar.getInstance()
    }
}

@Composable
fun LunarCalendarBentoScreen(calendar: Calendar, modifier: Modifier = Modifier) {
    // Current live time to keep the screen active and responsive
    var currentTimeString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            currentTimeString = sdf.format(now.time)
            delay(1000)
        }
    }

    // Solar Date decomposition
    val solarDay = calendar.get(Calendar.DAY_OF_MONTH)
    val solarMonth = calendar.get(Calendar.MONTH) + 1
    val solarYear = calendar.get(Calendar.YEAR)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // Calculate Lunar Date using the Ho Ngoc Duc algorithm
    val lunarResult = LunarCalendar.convertSolar2Lunar(solarDay, solarMonth, solarYear)
    val lunarDay = lunarResult[0]
    val lunarMonth = lunarResult[1]
    val lunarYear = lunarResult[2]
    val isLeapMonth = lunarResult[3] == 1

    val lunarYearName = LunarCalendar.getYearCanChi(lunarYear)

    val dayOfWeekVietnamese = when (dayOfWeek) {
        Calendar.SUNDAY -> "Chủ Nhật"
        Calendar.MONDAY -> "Thứ Hai"
        Calendar.TUESDAY -> "Thứ Ba"
        Calendar.WEDNESDAY -> "Thứ Tư"
        Calendar.THURSDAY -> "Thứ Năm"
        Calendar.FRIDAY -> "Thứ Sáu"
        Calendar.SATURDAY -> "Thứ Bảy"
        else -> ""
    }

    // Main layout splitting the screen horizontally (Top: Solar, Bottom: Lunar)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // --- TOP HALF: SOLAR CALENDAR (Light blue background, easy on the eyes) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Solar section header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DƯƠNG LỊCH ☀️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1),
                        letterSpacing = 2.sp
                    )
                    // Display Live Time inside the header
                    Text(
                        text = currentTimeString,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0369A1),
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bento Grid for Solar Date
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left giant card: Day Number + Day of the week
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .shadow(4.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFF0284C7), RoundedCornerShape(24.dp))
                            .testTag("solar_day_card")
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = String.format("%02d", solarDay),
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E3A8A),
                                lineHeight = 80.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dayOfWeekVietnamese,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Right column: Month Card & Year Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Month card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                                .testTag("solar_month_card")
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "THÁNG",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = solarMonth.toString(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }

                        // Year card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                                .testTag("solar_year_card")
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NĂM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = solarYear.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Divider strip between sections
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFFE2E8F0))
        )

        // --- BOTTOM HALF: LUNAR CALENDAR (Soft red/orange background, easy on the eyes) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Lunar section header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ÂM LỊCH 🌙",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBE123C),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "HOÀNG ĐẠO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBE123C),
                        modifier = Modifier
                            .background(Color(0xFFFFE4E6), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bento Grid for Lunar Date
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left giant card: Lunar Day Number
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .shadow(4.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFE11D48), RoundedCornerShape(24.dp))
                            .testTag("lunar_day_card")
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = String.format("%02d", lunarDay),
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF9F1239),
                                lineHeight = 80.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NGÀY ÂM",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE11D48),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Right column: Lunar Month & Lunar Year Can Chi
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Lunar Month card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFFDA4AF), RoundedCornerShape(20.dp))
                                .testTag("lunar_month_card")
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "THÁNG ÂM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48),
                                    letterSpacing = 1.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lunarMonth.toString(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF9F1239)
                                    )
                                    if (isLeapMonth) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Nhuận",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }
                        }

                        // Lunar Year Can Chi card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFFDA4AF), RoundedCornerShape(20.dp))
                                .testTag("lunar_year_card")
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NĂM ÂM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = lunarYearName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF9F1239),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

