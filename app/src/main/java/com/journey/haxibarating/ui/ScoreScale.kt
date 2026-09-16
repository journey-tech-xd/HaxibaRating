package com.journey.haxibarating.ui

import androidx.compose.ui.graphics.Color
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * The app's single red -> orange -> yellow -> light-green -> green scale,
 * used everywhere a 0..10 score needs a color: the rating sliders, the
 * per-category bars, and the big overall number. Keeping one function
 * means every screen reads the same way.
 */
object ScoreScale {
    private val stops = listOf(
        Color(0xFFE5484D), // 0  - terrible
        Color(0xFFE8825A), // ~2.5 - bad
        Color(0xFFEFC23D), // ~5  - okay
        Color(0xFF9AC93E), // ~7.5 - good
        Color(0xFF3FA65A)  // 10 - excellent
    )

    /** Interpolates a color for any value in 0..10 -- a 6.8 lands between
     *  "Okay" and "Good" rather than snapping to one bucket. Only call this
     *  once you know a score exists (ratingCount > 0); 0 is a real rating
     *  here, not a "no data" sentinel. */
    fun colorFor(score: Double): Color {
        val s = score.coerceIn(0.0, 10.0) / 10.0 * (stops.size - 1)
        val idx = floor(s).toInt().coerceIn(0, stops.size - 2)
        val t = (s - idx).toFloat()
        val a = stops[idx]
        val b = stops[idx + 1]
        return Color(
            red = a.red + (b.red - a.red) * t,
            green = a.green + (b.green - a.green) * t,
            blue = a.blue + (b.blue - a.blue) * t,
            alpha = 1f
        )
    }

    /** Same "score is known to exist" caveat as [colorFor]. */
    fun labelFor(score: Double): String = when {
        score < 2 -> "Terrible"
        score < 4 -> "Bad"
        score < 6 -> "Okay"
        score < 8 -> "Good"
        else -> "Excellent"
    }

    fun colorForInt(score: Int): Color = colorFor(score.toDouble())

    fun roundedPercent(score: Double): Int = ((score.coerceIn(0.0, 10.0) / 10.0) * 100).roundToInt()
}
