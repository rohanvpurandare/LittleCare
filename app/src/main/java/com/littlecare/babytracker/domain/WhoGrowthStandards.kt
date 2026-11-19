package com.littlecare.babytracker.domain

object WhoGrowthStandards {

    data class GrowthBandPoint(
        val week: Int,
        val p15: Float,
        val median: Float,
        val p85: Float
    )

    val weightReference: List<GrowthBandPoint> = listOf(
        GrowthBandPoint(0, 2.9f, 3.3f, 3.8f),
        GrowthBandPoint(4, 3.9f, 4.4f, 5.0f),
        GrowthBandPoint(8, 4.9f, 5.5f, 6.2f),
        GrowthBandPoint(12, 5.7f, 6.3f, 7.1f),
        GrowthBandPoint(16, 6.3f, 6.9f, 7.8f),
        GrowthBandPoint(20, 6.8f, 7.4f, 8.3f),
        GrowthBandPoint(24, 7.2f, 7.8f, 8.8f),
        GrowthBandPoint(28, 7.5f, 8.2f, 9.2f),
        GrowthBandPoint(32, 7.8f, 8.5f, 9.6f),
        GrowthBandPoint(36, 8.1f, 8.8f, 9.9f),
        GrowthBandPoint(40, 8.3f, 9.1f, 10.2f),
        GrowthBandPoint(44, 8.5f, 9.3f, 10.5f),
        GrowthBandPoint(48, 8.7f, 9.5f, 10.8f),
        GrowthBandPoint(52, 8.9f, 9.7f, 11.0f)
    )

    val heightReference: List<GrowthBandPoint> = listOf(
        GrowthBandPoint(0, 48.0f, 49.5f, 51.2f),
        GrowthBandPoint(4, 52.5f, 54.2f, 56.0f),
        GrowthBandPoint(8, 56.2f, 58.0f, 59.9f),
        GrowthBandPoint(12, 59.1f, 61.0f, 63.0f),
        GrowthBandPoint(16, 61.5f, 63.4f, 65.5f),
        GrowthBandPoint(20, 63.6f, 65.5f, 67.7f),
        GrowthBandPoint(24, 65.3f, 67.3f, 69.5f),
        GrowthBandPoint(28, 66.9f, 68.9f, 71.2f),
        GrowthBandPoint(32, 68.3f, 70.4f, 72.7f),
        GrowthBandPoint(36, 69.7f, 71.8f, 74.2f),
        GrowthBandPoint(40, 70.9f, 73.1f, 75.5f),
        GrowthBandPoint(44, 72.1f, 74.3f, 76.8f),
        GrowthBandPoint(48, 73.2f, 75.5f, 78.0f),
        GrowthBandPoint(52, 74.2f, 76.6f, 79.2f)
    )

    fun getInterpolatedMedian(week: Int, isWeight: Boolean): Float {
        val list = if (isWeight) weightReference else heightReference
        val clampedWeek = week.coerceIn(0, 52)
        val lower = list.lastOrNull { it.week <= clampedWeek } ?: list.first()
        val upper = list.firstOrNull { it.week >= clampedWeek } ?: list.last()
        if (lower.week == upper.week) return lower.median
        val ratio = (clampedWeek - lower.week).toFloat() / (upper.week - lower.week).toFloat()
        return lower.median + (upper.median - lower.median) * ratio
    }
}
