package com.littlecare.babytracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlecare.babytracker.domain.WhoGrowthStandards
import com.littlecare.babytracker.ui.theme.AccentFeed
import com.littlecare.babytracker.ui.theme.AccentPee
import com.littlecare.babytracker.ui.theme.OledBlack
import com.littlecare.babytracker.ui.theme.OledBorder
import com.littlecare.babytracker.ui.theme.TextSecondary

@Composable
fun GrowthChart(
    referenceBand: List<WhoGrowthStandards.GrowthBandPoint>,
    babyDataPoints: List<Pair<Int, Float>>,
    unitLabel: String,
    accentColor: Color = AccentFeed,
    modifier: Modifier = Modifier
) {
    val minVal = (referenceBand.minOfOrNull { it.p15 } ?: 0f) * 0.92f
    val maxVal = (referenceBand.maxOfOrNull { it.p85 } ?: 12f) * 1.06f
    val valRange = (maxVal - minVal).coerceAtLeast(1f)

    OledCard(modifier = modifier, glowColor = accentColor) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "WHO Growth Curve (0–52 Weeks)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = unitLabel.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(vertical = 8.dp)
        ) {
            val w = size.width
            val h = size.height

            fun xForWeek(week: Int): Float = (week.coerceIn(0, 52) / 52f) * w
            fun yForVal(v: Float): Float = h - (((v - minVal) / valRange) * h).coerceIn(0f, h)

            for (i in 0..4) {
                val y = (h / 4f) * i
                drawLine(
                    color = OledBorder.copy(alpha = 0.7f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (referenceBand.size >= 2) {
                val bandPath = Path().apply {
                    referenceBand.forEachIndexed { idx, pt ->
                        val x = xForWeek(pt.week)
                        val y = yForVal(pt.p85)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    referenceBand.asReversed().forEach { pt ->
                        lineTo(xForWeek(pt.week), yForVal(pt.p15))
                    }
                    close()
                }
                drawPath(
                    path = bandPath,
                    color = AccentPee.copy(alpha = 0.12f)
                )

                val medianPath = Path().apply {
                    referenceBand.forEachIndexed { idx, pt ->
                        val x = xForWeek(pt.week)
                        val y = yForVal(pt.median)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = medianPath,
                    color = AccentPee.copy(alpha = 0.65f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                )
            }

            val sortedPoints = babyDataPoints.sortedBy { it.first }
            if (sortedPoints.size >= 2) {
                val areaPath = Path().apply {
                    sortedPoints.forEachIndexed { idx, (wk, value) ->
                        val x = xForWeek(wk)
                        val y = yForVal(value)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    lineTo(xForWeek(sortedPoints.last().first), h)
                    lineTo(xForWeek(sortedPoints.first().first), h)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            accentColor.copy(alpha = 0.0f)
                        )
                    )
                )

                val babyPath = Path().apply {
                    sortedPoints.forEachIndexed { idx, (wk, value) ->
                        val x = xForWeek(wk)
                        val y = yForVal(value)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = babyPath,
                    color = accentColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            sortedPoints.forEach { (wk, value) ->
                val center = Offset(xForWeek(wk), yForVal(value))
                drawCircle(
                    color = accentColor.copy(alpha = 0.28f),
                    radius = 10.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = OledBlack,
                    radius = 5.5.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = accentColor,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Birth", fontSize = 12.sp, color = TextSecondary)
            Text("13w", fontSize = 12.sp, color = TextSecondary)
            Text("26w", fontSize = 12.sp, color = TextSecondary)
            Text("39w", fontSize = 12.sp, color = TextSecondary)
            Text("52w", fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("● Baby", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
            Text("--- WHO Median", fontSize = 12.sp, color = AccentPee)
            Text("■ WHO 15–85th %ile", fontSize = 12.sp, color = AccentPee.copy(alpha = 0.7f))
        }
    }
}
