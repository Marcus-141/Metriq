package com.application.metriq.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.metriq.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun VolumeChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier) {
            Text("No data available", color = TextGray)
        }
        return
    }

    val density = LocalDensity.current
    val textPaint = remember {
        Paint().apply {
            color = TextGray.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingBottom = 20.dp.toPx()
        val paddingLeft = 40.dp.toPx()
        val paddingRight = 20.dp.toPx()
        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingBottom

        val maxVolume = data.maxOfOrNull { it.second } ?: 1f
        // Add some headroom
        val maxY = if (maxVolume == 0f) 100f else maxVolume * 1.1f

        // Draw Y-Axis Labels & Grid Lines
        val steps = 4
        for (i in 0..steps) {
            val yVal = maxY * (i.toFloat() / steps)
            val yPos = graphHeight - (i.toFloat() / steps) * graphHeight
            
            // Draw grid line
            drawLine(
                color = TextGray.copy(alpha = 0.2f),
                start = Offset(paddingLeft, yPos),
                end = Offset(width, yPos),
                strokeWidth = 1f
            )

            // Draw label
            val label = if (maxY >= 1000) {
                "${(yVal / 1000).roundToInt()}k"
            } else {
                "${yVal.roundToInt()}"
            }
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    label,
                    paddingLeft / 2, 
                    yPos + textPaint.textSize / 3, 
                    textPaint
                )
            }
        }

        // Draw Line Chart
        val path = Path()
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, pair ->
            val xPos = paddingLeft + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * graphWidth
            val yPos = graphHeight - (pair.second / maxY) * graphHeight
            points.add(Offset(xPos, yPos))
            
            if (index == 0) {
                path.moveTo(xPos, yPos)
            } else {
                path.lineTo(xPos, yPos)
            }
            
            // Draw X-Axis Label
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    pair.first,
                    xPos,
                    height,
                    textPaint
                )
            }
        }

        drawPath(
            path = path,
            color = LogoCyan,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = LogoCyan,
                center = point,
                radius = 4.dp.toPx()
            )
            drawCircle(
                color = Color.Black, // Center cutout for style
                center = point,
                radius = 2.dp.toPx()
            )
        }
    }
}

