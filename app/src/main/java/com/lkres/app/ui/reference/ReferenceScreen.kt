package com.lkres.app.ui.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import com.lkres.app.core.ColorCode
import com.lkres.app.core.Eia96
import com.lkres.app.core.ResistorFormat

@Composable
fun ReferenceScreen() {
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle("Bảng màu chuẩn") }
        items(BandColor.entries.sortedBy { it.ordinal }) { c ->
            val digit = ColorCode.DIGITS[c]?.toString() ?: "—"
            val tol = ColorCode.TOLERANCES[c]?.let { "±${trim(it)}%" } ?: "—"
            val tcr = ColorCode.TCR[c]?.let { "$it ppm/°C" } ?: "—"
            ColorRow(
                c, listOf(
                    "Digit: $digit",
                    "Nhân: ×${trim(ColorCode.MULTIPLIERS[c])}",
                    "Dung sai: $tol",
                    "TCR: $tcr"
                ).joinToString("  ·  ")
            )
        }
        item { SectionTitle("Dung sai theo màu") }
        items(ColorCode.TOLERANCES.entries.sortedBy { it.value }) { (c, t) ->
            ColorRow(c, "±${trim(t)}%")
        }
        item { SectionTitle("Hệ số nhiệt TCR (dải 6)") }
        items(ColorCode.TCR.entries.sortedBy { it.value }) { (c, p) ->
            ColorRow(c, "$p ppm/°C")
        }
        item { SectionTitle("Bảng EIA-96") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Giá trị gốc (2 chữ số) × hệ số chữ cái:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Eia96.MULTIPLIERS.entries.sortedBy { it.key }.chunked(4).forEach { chunk ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        chunk.forEach { (letter, mult) ->
                            Text("$letter: ×${trim(mult)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        items(Eia96.VALUES.keys.sorted()) { code ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(code, fontWeight = FontWeight.Medium)
                Text(ResistorFormat.format(Eia96.VALUES.getValue(code).toDouble()))
            }
        }
    }
}

private fun trim(v: Double?): String {
    if (v == null) return "?"
    return if (v >= 1.0 && v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
}

@Composable
private fun SectionTitle(t: String) {
    Text(t, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun ColorRow(c: BandColor, detail: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        Arrangement.spacedBy(8.dp),
        Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(20.dp).background(Color(c.argb), RoundedCornerShape(4.dp))
        )
        Text("${c.label} — $detail", style = MaterialTheme.typography.bodyMedium)
    }
}
