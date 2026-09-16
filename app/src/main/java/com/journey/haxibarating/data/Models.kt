package com.journey.haxibarating.data

/**
 * One restaurant, with running totals instead of individual rating rows.
 * Averages are computed on-device from these totals (sum / count), which
 * keeps reads/writes cheap on Firebase's free tier: adding a rating is a
 * single atomic increment, and showing the list never has to fetch every
 * individual rating.
 */
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val mapsUrl: String = "",
    val createdAt: Long = 0L,
    val ratingCount: Long = 0L,
    val sumTaste: Long = 0L,
    val sumAmbience: Long = 0L,
    val sumQuality: Long = 0L,
    val sumService: Long = 0L
) {
    val avgTaste: Double get() = average(sumTaste)
    val avgAmbience: Double get() = average(sumAmbience)
    val avgQuality: Double get() = average(sumQuality)
    val avgService: Double get() = average(sumService)

    /** Overall score: the average of the four category averages, 0.0..10.0. */
    val overallAverage: Double
        get() {
            if (ratingCount == 0L) return 0.0
            return (avgTaste + avgAmbience + avgQuality + avgService) / 4.0
        }

    private fun average(sum: Long): Double =
        if (ratingCount == 0L) 0.0 else sum.toDouble() / ratingCount.toDouble()
}

/** Every category is rated on a plain 0..10 scale (an NPS-style slider in
 *  the UI), so a rating is just four integers -- no more discrete levels. */
const val RATING_MIN = 0
const val RATING_MAX = 10

enum class Category(val label: String) {
    TASTE("Taste"),
    AMBIENCE("Ambience"),
    QUALITY("Quality"),
    SERVICE("Service")
}
