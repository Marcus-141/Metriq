package com.application.metriq.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.core.graphics.PathParser
import com.application.metriq.data.FrontBodyAssets
import com.application.metriq.data.FrontMuscleGroup

@Composable
fun FrontMuscleHeatMap(
    modifier: Modifier = Modifier,
    recoveryScores: Map<FrontMuscleGroup, Float> // 0.0 (Fresh) to 1.0 (Sore)
) {
    // Convert the SVG Strings to Android Paths (Cached)
    val musclePaths = remember {
        FrontBodyAssets.muscles.map { muscle ->
            // Parse the string data into RearMuscleData.kt
            val androidPath = PathParser.createPathFromPathData(muscle.pathData)
            muscle.id to androidPath.asComposePath()
        }
    }

    val viewportWidth = 1024f
    val viewportHeight = 1024f
    val zoomFactor = 1.4f

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scale the drawing to fit the screen
            val scaleX = size.width / viewportWidth
            val scaleY = size.height / viewportHeight
            val baseScale = minOf(scaleX, scaleY)
            val finalScale = baseScale * zoomFactor

            // Calculate translation to center the zoomed map
            val scaledWidth = viewportWidth * finalScale
            val scaledHeight = viewportHeight * finalScale

            val translateX = (size.width - scaledWidth) / 2f
            val translateY = (size.height - scaledHeight) / 2f

            translate(left = translateX, top = translateY) {
                scale(scale = finalScale, pivot = Offset.Zero) {
                    musclePaths.forEach { (id, path) ->

                        // Get the color based on the score
                        val soreness = recoveryScores[id] ?: 0f
                        val color = getHeatMapColor(soreness)

                        drawPath(
                            path = path,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

