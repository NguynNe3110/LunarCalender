package com.example.utils

import kotlin.math.*

object LunarCalendar {

    // Julian Day from Date (Solar)
    fun jdFromDate(d: Int, m: Int, y: Int): Double {
        var mm = m
        var yy = y
        if (mm <= 2) {
            yy -= 1
            mm += 12
        }
        val a = yy / 100
        val b = if (yy > 1582 || (yy == 1582 && mm > 10) || (yy == 1582 && mm == 10 && d >= 15)) {
            val b1 = a / 4
            2 - a + b1
        } else {
            0
        }
        val c = floor(365.25 * yy).toInt()
        val e = floor(30.6001 * (mm + 1)).toInt()
        return (b + c + e + d + 1720995).toDouble()
    }

    // Julian Day of the k-th New Moon (Astronomical)
    fun getNewMoonDay(k: Double, timeZone: Double = 7.0): Double {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        // Mean New Moon JDN
        var jd = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3 + 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * PI / 180.0)

        // Sun's mean anomaly
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3 + 0.00000010 * t4
        // Moon's mean anomaly
        val mprime = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3 - 0.00000110 * t4
        // Moon's mean argument of latitude
        val f = 21.2964 + 390.67050274 * k - 0.0016528 * t2 - 0.00000239 * t3 + 0.00000011 * t4

        val mRad = m * PI / 180.0
        val mprimeRad = mprime * PI / 180.0
        val fRad = f * PI / 180.0

        val dJd = (0.1734 - 0.000393 * t) * sin(mRad) +
                0.0021 * sin(2 * mRad) -
                0.4068 * sin(mprimeRad) +
                0.0161 * sin(2 * mprimeRad) -
                0.0004 * sin(3 * mprimeRad) +
                0.0104 * sin(2 * fRad) -
                0.0051 * sin(mRad + mprimeRad) -
                0.0074 * sin(mRad - mprimeRad) +
                0.0004 * sin(2 * fRad + mRad) -
                0.0004 * sin(2 * fRad - mRad) -
                0.0006 * sin(2 * fRad + mprimeRad) -
                0.0016 * sin(2 * fRad - mprimeRad) +
                0.0104 * sin(2 * mRad + mprimeRad)

        jd += dJd
        return floor(jd + 0.5 + timeZone / 24.0)
    }

    // Sun's Longitude in degrees
    fun getSunLongitude(jd: Double, timeZone: Double = 7.0): Double {
        val t = (jd - 2451545.0 - timeZone / 24.0) / 36525.0
        val t2 = t * t
        val t3 = t2 * t

        val g = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t3
        val l = 280.46646 + 36000.76983 * t + 0.0003032 * t2

        val gRad = g * PI / 180.0
        val dl = (1.914602 - 0.004817 * t - 0.000014 * t2) * sin(gRad) +
                (0.019993 - 0.000101 * t) * sin(2 * gRad) +
                0.000289 * sin(3 * gRad)

        var trueLong = l + dl
        trueLong %= 360.0
        if (trueLong < 0) trueLong += 360.0
        return trueLong
    }

    // Convert Solar date (dd, mm, yyyy) to Lunar date (day, month, year, isLeap)
    // Returns IntArray of size 4: [day, month, year, isLeap (0: false, 1: true)]
    fun convertSolar2Lunar(dd: Int, mm: Int, yyyy: Int, timeZone: Double = 7.0): IntArray {
        val jd = jdFromDate(dd, mm, yyyy)
        val k = floor((yyyy - 1900) * 12.3685).toInt()

        // Find New Moon k0 containing jd
        var k0 = k
        var nm = getNewMoonDay(k0.toDouble(), timeZone)
        if (nm > jd) {
            while (nm > jd) {
                k0--
                nm = getNewMoonDay(k0.toDouble(), timeZone)
            }
        } else {
            while (getNewMoonDay((k0 + 1).toDouble(), timeZone) <= jd) {
                k0++
            }
            nm = getNewMoonDay(k0.toDouble(), timeZone)
        }

        val day = (jd - nm).toInt() + 1

        // Find the Winter Solstice (Sun Longitude = 270) containing month 11 of year yyyy-1
        val jdwPrev = jdFromDate(22, 12, yyyy - 1)
        var kPrev = floor(((yyyy - 1) - 1900) * 12.3685).toInt()
        while (getNewMoonDay(kPrev.toDouble(), timeZone) > jdwPrev) {
            kPrev--
        }
        while (getNewMoonDay((kPrev + 1).toDouble(), timeZone) <= jdwPrev) {
            kPrev++
        }

        // Find the Winter Solstice containing month 11 of year yyyy
        val jdwCurr = jdFromDate(22, 12, yyyy)
        var kCurr = floor((yyyy - 1900) * 12.3685).toInt()
        while (getNewMoonDay(kCurr.toDouble(), timeZone) > jdwCurr) {
            kCurr--
        }
        while (getNewMoonDay((kCurr + 1).toDouble(), timeZone) <= jdwCurr) {
            kCurr++
        }

        val numMonths = kCurr - kPrev
        var leapMonthIdx = -1

        if (numMonths == 13) {
            for (ki in kPrev + 1..kCurr) {
                val nmStart = getNewMoonDay(ki.toDouble(), timeZone)
                val nmEnd = getNewMoonDay((ki + 1).toDouble(), timeZone)
                val sStart = getSunLongitude(nmStart, timeZone)
                val sEnd = getSunLongitude(nmEnd, timeZone)
                val termStart = floor(sStart / 30.0).toInt()
                val termEnd = floor(sEnd / 30.0).toInt()
                if (termStart == termEnd) {
                    leapMonthIdx = ki
                    break
                }
            }
        }

        var month = 11 + (k0 - kPrev)
        var isLeap = 0

        if (numMonths == 13) {
            if (k0 == leapMonthIdx) {
                isLeap = 1
                month = 11 + (k0 - kPrev) - 1
            } else if (k0 > leapMonthIdx) {
                month = 11 + (k0 - kPrev) - 1
            }
        }

        month = (month - 1) % 12 + 1
        if (month <= 0) month += 12

        // Determine lunar year change at Lunar Month 1
        var kNewYear = kPrev + 2
        var mTemp = 11 + (kNewYear - kPrev)
        if (numMonths == 13 && kNewYear > leapMonthIdx) {
            mTemp--
        }
        mTemp = (mTemp - 1) % 12 + 1
        if (mTemp != 1) {
            kNewYear++
        }

        val lunarYear = if (k0 < kNewYear) yyyy - 1 else yyyy

        return intArrayOf(day, month, lunarYear, isLeap)
    }

    // Get Year Can Chi representation
    fun getYearCanChi(lunarYear: Int): String {
        val canArray = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
        val chiArray = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
        val can = canArray[(lunarYear + 6) % 10]
        val chi = chiArray[(lunarYear + 8) % 12]
        return "$can $chi"
    }
}
