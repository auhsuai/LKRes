package com.lkres.app.ui.common

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Nút đổi chế độ Trở cắm <-> Trở dán. Icon mũi tên cong ↻ tự vẽ bằng
// ImageVector.Builder — cùng style với 4 icon tab trong LKResApp.kt
// (path đơn giản, SolidColor(Color.Black) để Icon tự tint theo theme).
private fun swapIcon(): ImageVector = ImageVector.Builder(
    name = "SwapViewIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(fill = SolidColor(Color.Black)) {
    moveTo(16.5f, 4.21f)
    arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 12f, y1 = 3f)
    lineTo(10.9f, 1.1f)
    lineTo(16.8f, 4.1f)
    lineTo(10.9f, 7.1f)
    lineTo(12f, 5.2f)
    arcTo(6.8f, 6.8f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 15.4f, y1 = 6.11f)
    close()
}.build()

@Composable
fun SwapViewButton(contentDescription: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(swapIcon(), contentDescription = contentDescription)
    }
}
