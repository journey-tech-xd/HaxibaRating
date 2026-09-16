package com.journey.haxibarating.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarDatum(val label: String, val value: Double, val color: Color)

/**
 * Simple horizontal bar chart for four 0.0..10.0 averages. Deliberately
 * drawn by hand with Canvas instead of a charting library, so the project
 * pulls in one less dependency that could fail to resolve on a first-time
 * Gradle sync.
 *
 * [hasData] is passed once for the whole chart rather than inferred per
 * bar: on the 0..10 scale, 0 is a real (if unflattering) rating, so it
 * can't double as a "nothing rated yet" sentinel the way it could on the
 * old 1..3 scale.
 */
@Composable
fun AverageBarChart(data: List<BarDatum>, hasData: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        data.forEach { datum ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = datum.label,
                    modifier = Modifier.width(84.dp),
                    fontSize = 13.sp,
                    color = Color(0xFFF1F4FA)
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                ) {
                    val trackColor = Color(0x1AFFFFFF)
                    val fraction = (datum.value.coerceIn(0.0, 10.0) / 10.0).toFloat()
                    drawRoundRect(color = trackColor, cornerRadius = androidx.compose.ui.geometry.CornerRadius(9.dp.toPx()))
                    if (hasData) {
                        drawRoundRect(
                            color = datum.color,
                            size = size.copy(width = size.width * fraction.coerceIn(0.02f, 1f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(9.dp.toPx())
                        )
                    }
                }
                Text(
                    text = if (hasData) String.format("%.1f", datum.value) else "–",
                    modifier = Modifier
                        .width(32.dp)
                        .padding(start = 6.dp),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    color = Color(0xFF8D96AC)
                )
            }
        }
    }
}
