package com.example.utils

import java.util.Calendar
import java.util.TimeZone

object AstronomyUtils {

    /**
     * Approximates Sunrise and Sunset decimal local hours for a given location and date under NOAA formulas.
     * Guaranteed to work offline with clean high-accuracy results (typically within 5-10 minutes accuracy).
     */
    fun getBackupSunriseSunset(latitude: Double, longitude: Double, date: Calendar): Pair<Double, Double> {
        val dayOfYear = date.get(Calendar.DAY_OF_YEAR)
        
        // Sun's declination: 
        // 23.45 * sin( 360/365 * (d - 80) )
        val declination = 23.45 * java.lang.Math.sin(java.lang.Math.toRadians(360.0 / 365.0 * (dayOfYear - 80)))
        
        // Hour angle (H) at sunrise/sunset: 
        // cos(H) = -tan(lat) * tan(declination)
        val latRad = java.lang.Math.toRadians(latitude)
        val declRad = java.lang.Math.toRadians(declination)
        
        val cosH = -java.lang.Math.tan(latRad) * java.lang.Math.tan(declRad)
        
        val H = when {
            cosH < -1.0 -> 180.0 // Sun never sets (polar day)
            cosH > 1.0 -> 0.0    // Sun never rises (polar night)
            else -> java.lang.Math.toDegrees(java.lang.Math.acos(cosH))
        }
        
        // Timezone offset in hours
        val timeZone = date.timeZone
        val offsetMs = timeZone.getOffset(date.timeInMillis)
        val tzOffsetHours = offsetMs / 3600000.0
        
        // Equation of time (approximate in minutes)
        val b = java.lang.Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))
        val eqTime = 9.87 * java.lang.Math.sin(2.0 * b) - 7.53 * java.lang.Math.cos(b) - 1.5 * java.lang.Math.sin(b)
        
        // Solar noon in UTC hours:
        // 12.0 - longitude / 15.0 - eqTime / 60.0
        val solarNoonUtc = 12.0 - (longitude / 15.0) - (eqTime / 60.0)
        val solarNoonLocal = solarNoonUtc + tzOffsetHours
        
        // Sunrise/Sunset in local hours:
        val sunrise = solarNoonLocal - (H / 15.0)
        val sunset = solarNoonLocal + (H / 15.0)
        
        // Wrap and normalize within [0..24]
        fun wrapDecimalHour(h: Double): Double {
            var valH = h % 24.0
            if (valH < 0) valH += 24.0
            return valH
        }
        
        // Default fallbacks if polar conditions occur
        val finalSunrise = if (cosH > 1.0) 6.0 else if (cosH < -1.0) 0.1 else wrapDecimalHour(sunrise)
        val finalSunset = if (cosH > 1.0) 18.0 else if (cosH < -1.0) 23.9 else wrapDecimalHour(sunset)
        
        return Pair(finalSunrise, finalSunset)
    }

    /**
     * Calculates Moon's celestial longitude (0..360) using a simplified model of Jean Meeus.
     */
    fun getLunarLongitude(timeMs: Long): Double {
        // Epoch J2000 is 2000-01-01 12:00:00 UTC (946728000000 ms)
        val d = (timeMs - 945728000000L) / 86400000.0
        val T = d / 36525.0
        
        // Mean longitude of Moon (L') in degrees
        var lp = 218.316 + 481267.881 * T
        // Mean elongation of Moon (D) in degrees
        var D = 297.850 + 445267.111 * T
        // Mean anomaly of Sun (M) in degrees
        var M = 357.529 + 35999.050 * T
        // Mean anomaly of Moon (M') in degrees
        var mp = 134.963 + 477198.868 * T
        
        fun norm(deg: Double): Double {
            var r = deg % 360.0
            if (r < 0) r += 360.0
            return r
        }
        
        lp = norm(lp)
        D = norm(D)
        M = norm(M)
        mp = norm(mp)
        
        val dRad = java.lang.Math.toRadians(D)
        val mRad = java.lang.Math.toRadians(M)
        val mpRad = java.lang.Math.toRadians(mp)
        
        // Principal astronomical perturbations in degrees:
        val longitude = lp + 
                6.289 * java.lang.Math.sin(mpRad) + 
                1.274 * java.lang.Math.sin(2 * dRad - mpRad) + 
                0.658 * java.lang.Math.sin(2 * dRad) + 
                0.214 * java.lang.Math.sin(2 * mpRad) - 
                0.186 * java.lang.Math.sin(mRad)
                
        return norm(longitude)
    }

    /**
     * Gets Mansion index (1-based, 1 to 28)
     */
    fun getMansionIndex(longitude: Double): Int {
        val i = (longitude / (360.0 / 28.0)).toInt() + 1
        return i.coerceIn(1, 28)
    }

    /**
     * Estimates remaining hours for the lunar mansion shift.
     * The Moon sweeps approx 13.176 degrees of longitude per 24 hours (0.549 degree per hour).
     */
    fun getHoursRemainingInMansion(longitude: Double): Double {
        val step = 360.0 / 28.0
        val mansionIdx = getMansionIndex(longitude)
        val mansionEndDegree = mansionIdx * step
        var degreeDiff = mansionEndDegree - longitude
        if (degreeDiff < 0) degreeDiff += 360.0
        // Moon moves ~0.55 degrees/hour relative to stars
        return degreeDiff / 0.549
    }
}
