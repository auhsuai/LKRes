package com.lkres.app.ui.resistor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import kotlin.math.floor

private val BodyLight = Color(0xFFF7ECCB)
private val BodyBase = Color(0xFFE8D5A3)
private val BodyDark = Color(0xFFB99B62)
private val LeadLight = Color(0xFFDDE1E6)
private val LeadDark = Color(0xFF878E96)

private const val BODY_LEFT_F = 0.24f
private const val BODY_RIGHT_F = 0.76f
private const val BODY_TOP_F = 0.28f
private const val BODY_BOTTOM_F = 0.72f
private const val BAND_AREA_START_F = 0.12f
private const val BAND_AREA_SPAN_F = 0.76f

private const val ACTIVE_BORDER_WIDTH_DP = 2
private const val ACTIVE_BORDER_CORNER_DP = 3
private const val ACTIVE_BORDER_LIGHTEN_FRACTION = 0.45f
private const val EMPTY_BAND_BORDER_ALPHA = 0.7f

internal fun bandRectF(bandCount: Int, index: Int, w: Float, h: Float): Rect {
    val slot = BAND_AREA_SPAN_F / bandCount
    val bodyW = w * (BODY_RIGHT_F - BODY_LEFT_F)
    val left = w * BODY_LEFT_F + bodyW * (BAND_AREA_START_F + index * slot)
    val width = bodyW * slot * 0.55f
    return Rect(
        offset = Offset(left, h * BODY_TOP_F),
        size = Size(width, h * (BODY_BOTTOM_F - BODY_TOP_F))
    )
}

@Composable
fun ResistorCanvas(
    bandColors: List<BandColor?>,
    modifier: Modifier = Modifier,
    activeBandIndex: Int = -1,
    onBandTap: ((Int) -> Unit)? = null
) {
    Canvas(
        modifier.pointerInput(onBandTap, bandColors.size) {
            detectTapGestures { offset ->
                val handler = onBandTap ?: return@detectTapGestures
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                val n = bandColors.size
                if (n == 0) return@detectTapGestures
                if (offset.y < h * BODY_TOP_F || offset.y > h * BODY_BOTTOM_F) {
                    return@detectTapGestures
                }
                val rel = (offset.x - w * BODY_LEFT_F) / (w * (BODY_RIGHT_F - BODY_LEFT_F))
                val idx = floor((rel - BAND_AREA_START_F) / (BAND_AREA_SPAN_F / n)).toInt()
                if (idx in 0 until n) handler(idx)
            }
        }
    ) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val bodyTop = h * BODY_TOP_F
        val bodyBottom = h * BODY_BOTTOM_F
        val bodyH = bodyBottom - bodyTop
        val bodyLeft = w * BODY_LEFT_F
        val bodyRight = w * BODY_RIGHT_F
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
                val rect = bandRectF(n, index, w, h)
                val base = Color(bandColor.argb)
                clipPath(body) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to shade(base, 1.35f), 0.42f to base,
                            0.78f to shade(base, 0.55f), 1f to shade(base, 0.9f),
                            startY = bodyTop, endY = bodyBottom
                        ),
                        topLeft = rect.topLeft,
                        size = rect.size
                    )
                }
            }
        }

        if (activeBandIndex in bandColors.indices && n > 0) {
            val rect = bandRectF(n, activeBandIndex, w, h)
            val base = bandColors[activeBandIndex]?.let { Color(it.argb) }
            val borderColor = base?.let { lerp(it, Color.White, ACTIVE_BORDER_LIGHTEN_FRACTION) }
                ?: Color.White.copy(alpha = EMPTY_BAND_BORDER_ALPHA)
            clipPath(body) {
                drawRoundRect(
                    color = borderColor,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(ACTIVE_BORDER_CORNER_DP.dp.toPx()),
                    style = Stroke(width = ACTIVE_BORDER_WIDTH_DP.dp.toPx())
                )
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
