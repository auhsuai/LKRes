package com.lkres.app.ui.resistor

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import com.lkres.app.core.BandColor

private val BodyLight = Color(0xFFF7ECCB)
private val BodyBase = Color(0xFFE8D5A3)
private val BodyDark = Color(0xFFB99B62)
private val LeadLight = Color(0xFFDDE1E6)
private val LeadDark = Color(0xFF878E96)

@Composable
fun ResistorCanvas(bandColors: List<BandColor?>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val bodyTop = h * 0.28f
        val bodyBottom = h * 0.72f
        val bodyH = bodyBottom - bodyTop
        val bodyLeft = w * 0.24f
        val bodyRight = w * 0.76f
        val bodyW = bodyRight - bodyLeft

        val leadBrush = Brush.verticalGradient(
            listOf(LeadLight, LeadDark, LeadLight),
            startY = midY - bodyH * 0.07f, endY = midY + bodyH * 0.07f
        )
        val leadHalf = bodyH * 0.07f
        val leadInnerLeft = bodyLeft + bodyW * 0.10f
        val leadInnerRight = bodyRight - bodyW * 0.10f
        drawPath(leadPath(0f, leadInnerLeft, midY, leadHalf), leadBrush)
        drawPath(leadPath(leadInnerRight, w, midY, leadHalf), leadBrush)

        val bodyBrush = Brush.verticalGradient(
            0f to BodyLight, 0.42f to BodyBase, 0.78f to BodyDark, 1f to BodyBase,
            startY = bodyTop, endY = bodyBottom
        )
        val body = bodyPath(bodyLeft, bodyRight, bodyTop, bodyBottom, midY)
        drawPath(body, bodyBrush)

        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                startY = bodyTop, endY = bodyTop + bodyH * 0.4f
            ),
            topLeft = Offset(bodyLeft + bodyW * 0.06f, bodyTop + bodyH * 0.10f),
            size = Size(bodyW * 0.88f, bodyH * 0.22f),
            cornerRadius = CornerRadius(bodyH * 0.11f)
        )

        val n = bandColors.size
        bandColors.forEachIndexed { index, bandColor ->
            if (bandColor != null) {
                val slot = 0.76f / n
                val bx = bodyLeft + bodyW * (0.12f + index * slot)
                val bw = bodyW * slot * 0.55f
                val base = Color(bandColor.argb)
                clipPath(body) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to shade(base, 1.35f), 0.42f to base,
                            0.78f to shade(base, 0.55f), 1f to shade(base, 0.9f),
                            startY = bodyTop, endY = bodyBottom
                        ),
                        topLeft = Offset(bx, bodyTop),
                        size = Size(bw, bodyH)
                    )
                }
            }
        }

        clipPath(body) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent, 0.58f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.16f),
                    startY = bodyTop, endY = bodyBottom
                ),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyW, bodyH)
            )
        }
    }
}

private fun leadPath(outerX: Float, innerX: Float, midY: Float, halfWidth: Float): Path {
    val tipScale = 0.70f
    return Path().apply {
        moveTo(outerX, midY - halfWidth * tipScale)
        lineTo(innerX, midY - halfWidth)
        lineTo(innerX, midY + halfWidth)
        lineTo(outerX, midY + halfWidth * tipScale)
        close()
    }
}

private fun bodyPath(l: Float, r: Float, t: Float, b: Float, midY: Float): Path {
    val span = r - l
    val halfH = (b - t) / 2f
    return Path().apply {
        moveTo(l, midY)
        cubicTo(l + span * 0.02f, t + halfH * 0.30f, l + span * 0.14f, t, l + span * 0.22f, t)
        lineTo(r - span * 0.22f, t)
        cubicTo(r - span * 0.14f, t, r - span * 0.02f, t + halfH * 0.30f, r, midY)
        cubicTo(r - span * 0.02f, b - halfH * 0.30f, r - span * 0.14f, b, r - span * 0.22f, b)
        lineTo(l + span * 0.22f, b)
        cubicTo(l + span * 0.14f, b, l + span * 0.02f, b - halfH * 0.30f, l, midY)
        close()
    }
}

private fun shade(base: Color, factor: Float): Color = if (factor >= 1f) {
    Color(
        red = base.red + (1f - base.red) * (factor - 1f),
        green = base.green + (1f - base.green) * (factor - 1f),
        blue = base.blue + (1f - base.blue) * (factor - 1f),
        alpha = base.alpha
    )
} else {
    Color(base.red * factor, base.green * factor, base.blue * factor, base.alpha)
}
