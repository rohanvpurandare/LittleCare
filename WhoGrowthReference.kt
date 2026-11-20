package com.littlecare.babytracker.domain

/**
 * WHO Child Growth Standards (0-52 weeks / 1st year)
 * Reference median (50th percentile) & 3rd/97th bands for Weight (kg) and Length (cm).
 */
object WhoGrowthReference {

    // Anchors at weeks: 0, 4, 8, 13, 17, 22, 26, 30, 35, 39, 44, 48, 52
    private val anchorWeeks = listOf(0, 4, 8, 13, 17, 22, 26, 30, 35, 39, 44, 48, 52)

    private val boysWeight50 = listOf(3.3f, 4.5f, 5.6f, 6.4f, 7.0f, 7.5f, 7.9f, 8.3f, 8.6f, 8.9f, 9.2f, 9.4f, 9.6f)
    private val girlsWeight50 = listOf(3.2f, 4.2f, 5.1f, 5.8f, 6.4f, 6.9f, 7.3f, 7.6f, 7.9f, 8.2f, 8.5f, 8.7f, 8.9f)

    private val boysHeight50 = listOf(49.9f, 54.7f, 58.4f, 61.4f, 63.9f, 65.9f, 67.6f, 69.2f, 70.6f, 72.0f, 73.3f, 74.5f, 75.7f)
    private val girlsHeight50 = listOf(49.1f, 53.7f, 57.1f, 59.8f, 62.1f, 64.0f, 65.7f, 67.3f, 68.7f, 70.1f, 71.5f, 72.8f, 74.0f)

    fun expectedWeightKg(week: Int, isBoy: Boolean): Float {
        val list = if (isBoy) boysWeight50 else girlsWeight50
        return interpolate(week.coerceIn(0, 52), list)
    }

    fun expectedHeightCm(week: Int, isBoy: Boolean): Float {
        val list = if (isBoy) boysHeight50 else girlsHeight50
        return interpolate(week.coerceIn(0, 52), list)
    }

    private fun interpolate(week: Int, values: List<Float>): Float {
        for (i in 0 until anchorWeeks.lastIndex) {
            val w0 = anchorWeeks[i]
            val w1 = anchorWeeks[i + 1]
            if (week in w0..w1) {
                val fraction = (week - w0).toFloat() / (w1 - w0).toFloat()
                return values[i] + fraction * (values[i + 1] - values[i])
            }
        }
        return values.last()
    }
}
