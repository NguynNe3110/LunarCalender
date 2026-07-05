package com.uzuu.lunarcalender

import com.uzuu.lunarcalender.R
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.uzuu.lunarcalender.MainActivity
import com.example.utils.LunarCalendar
import java.util.Calendar

class LunarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Update widgets on time or timezone change to guarantee accurate dates
        if (intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, LunarWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.lunar_widget_layout)

            // Get current calendar
            val calendar = Calendar.getInstance()
            val solarDay = calendar.get(Calendar.DAY_OF_MONTH)
            val solarMonth = calendar.get(Calendar.MONTH) + 1
            val solarYear = calendar.get(Calendar.YEAR)

            // Convert solar to lunar
            val lunarResult = LunarCalendar.convertSolar2Lunar(solarDay, solarMonth, solarYear)
            val lunarDay = lunarResult[0]
            val lunarMonth = lunarResult[1]
            val lunarYear = lunarResult[2]
            val isLeapMonth = lunarResult[3] == 1

            val lunarYearName = LunarCalendar.getYearCanChi(lunarYear)

            // Bind lunar date text
            views.setTextViewText(
                R.id.widget_lunar_date,
                "${String.format("%02d", lunarDay)}/${String.format("%02d", lunarMonth)}/$lunarYear"
            )

//            // Bind lunar year name
//            val leapSuffix = if (isLeapMonth) " (Tháng Nhuận)" else ""
//            views.setTextViewText(R.id.widget_lunar_year, "Năm $lunarYearName$leapSuffix")

            // Setup click pending intent to launch MainActivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        // Helper to manually trigger an update of all widgets (e.g., from MainActivity)
        fun forceUpdateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, LunarWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val intent = Intent(context, LunarWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
