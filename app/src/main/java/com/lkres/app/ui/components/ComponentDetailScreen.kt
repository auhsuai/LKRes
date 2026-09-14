package com.lkres.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkres.app.core.Component

private val BodyColor = Color(0xFF1C1C1E)
private val BodyBorder = Color(0xFF4A4A4E)
private val LeadColor = Color(0xFF9EA3A8)

private const val BODY_LEFT_F = 0.32f
private const val BODY_RIGHT_F = 0.68f
private const val BODY_TOP_F = 0.08f
private const val BODY_BOTTOM_F = 0.62f
private const val SHOULDER_F = 0.45f
private const val LEG_BOTTOM_F = 0.82f
private const val NAME_FONT_SP = 20f
private const val PIN_FONT_SP = 14f
private const val LEG_WIDTH_DP = 4.0
private const val BORDER_WIDTH_DP = 1.5
private const val PIN_LABEL_GAP_DP = 8.0

@Composable
fun ComponentDetailScreen(component: Component, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(backArrowIcon(), contentDescription = "Quay lại")
            }
            Text(component.name, style = MaterialTheme.typography.headlineSmall)
        }

        Text(component.fullName)
        Text(
            "${component.kind} · ${component.packageName}",
            style = MaterialTheme.typography.bodyMedium,
        )

        TO92Canvas(
            component = component,
            modifier = Modifier.fillMaxWidth().height(200.dp),
        )

        Text("Nhìn mặt có chữ, chân trái → phải", style = MaterialTheme.typography.bodySmall)

        component.pins.forEachIndexed { index, pin ->
            Text("${positionLabel(index)}: ${pin.symbol} — ${pin.function}")
        }
    }
}

private fun positionLabel(index: Int): String = when (index) {
    0 -> "Chân trái"
    1 -> "Chân giữa"
    else -> "Chân phải"
}

// Hình TO-92 nhìn thẳng mặt có chữ: mã in trên thân, 3 chân hướng xuống,
// nhãn symbol (E/C/B hoặc S/G/D) lấy từ component.pins — không hard-code.
@Composable
private fun TO92Canvas(component: Component, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    // Lấy màu từ theme NGOÀI Canvas (DrawScope không có CompositionLocal).
    val pinLabelColor = MaterialTheme.colorScheme.onSurface
    val nameStyle = TextStyle(
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = NAME_FONT_SP.sp,
    )
    val pinLabelStyle = TextStyle(color = pinLabelColor, fontSize = PIN_FONT_SP.sp)

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bodyLeft = w * BODY_LEFT_F
        val bodyRight = w * BODY_RIGHT_F
        val bodyTop = h * BODY_TOP_F
        val bodyBottom = h * BODY_BOTTOM_F
        val bodyW = bodyRight - bodyLeft
        val bodyCenterX = (bodyLeft + bodyRight) / 2f
        val shoulderY = bodyTop + (bodyBottom - bodyTop) * SHOULDER_F

        // Thân: đáy phẳng, hai vai bo tròn lên đỉnh (silhouette TO-92).
        val body = Path().apply {
            moveTo(bodyLeft, bodyBottom)
            lineTo(bodyLeft, shoulderY)
            quadraticBezierTo(bodyLeft, bodyTop, bodyCenterX, bodyTop)
            quadraticBezierTo(bodyRight, bodyTop, bodyRight, shoulderY)
            lineTo(bodyRight, bodyBottom)
            close()
        }
        drawPath(body, BodyColor)
        drawPath(body, BodyBorder, style = Stroke(width = BORDER_WIDTH_DP.dp.toPx()))

        // Chân hướng xuống: chia đều chiều rộng thân (3 chân -> 25% / 50% / 75%).
        val legBottom = h * LEG_BOTTOM_F
        val pinCount = component.pins.size
        val legXs = List(pinCount) { i ->
            bodyLeft + bodyW * (i + 1).toFloat() / (pinCount + 1).toFloat()
        }
        legXs.forEach { x ->
            drawLine(
                color = LeadColor,
                start = Offset(x, bodyBottom),
                end = Offset(x, legBottom),
                strokeWidth = LEG_WIDTH_DP.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Mã linh kiện in trên mặt phẳng của thân, canh giữa ngang + giữa vùng mặt.
        val nameLayout = textMeasurer.measure(AnnotatedString(component.name), style = nameStyle)
        drawText(
            textLayoutResult = nameLayout,
            topLeft = Offset(
                x = bodyCenterX - nameLayout.size.width / 2f,
                y = (shoulderY + bodyBottom) / 2f - nameLayout.size.height / 2f,
            ),
        )

        // Nhãn chân ngay dưới mỗi chân, canh giữa theo chân.
        component.pins.forEachIndexed { index, pin ->
            val layout = textMeasurer.measure(AnnotatedString(pin.symbol), style = pinLabelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = legXs[index] - layout.size.width / 2f,
                    y = legBottom + PIN_LABEL_GAP_DP.dp.toPx(),
                ),
            )
        }
    }
}
