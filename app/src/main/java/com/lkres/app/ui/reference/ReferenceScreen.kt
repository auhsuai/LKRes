package com.lkres.app.ui.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkres.app.core.BandColor
import com.lkres.app.core.ColorCode
import com.lkres.app.core.Eia96
import com.lkres.app.core.ResistorCalculator
import com.lkres.app.core.ResistorFormat

@Composable
fun ReferenceScreen() {
    val eia96Rows = remember {
        Eia96.VALUES.keys.sorted().chunked(3).map { chunk ->
            chunk.map { code ->
                code to ResistorFormat.format(Eia96.VALUES.getValue(code).toDouble())
            }
        }
    }
    val eia96LegendRows = remember {
        Eia96.MULTIPLIERS.entries.sortedBy { it.key }.chunked(4)
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle("Bảng màu chuẩn") }
        item {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(40.dp))
                CellText("Tên", Modifier.weight(1.2f), header = true)
                CellText("Giá trị", Modifier.weight(0.8f), header = true)
                CellText("Nhân", Modifier.weight(1.45f), header = true)
                CellText("Dung sai", Modifier.weight(1.1f), header = true)
                CellText("Hệ số nhiệt", Modifier.weight(1.1f), header = true)
            }
        }
        items(BandColor.entries.sortedBy { it.ordinal }) { c ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                Swatch(c.argb)
                CellText(c.label, Modifier.weight(1.2f))
                CellText(ColorCode.DIGITS[c]?.toString() ?: "-", Modifier.weight(0.8f))
                CellText("×${trim(ColorCode.MULTIPLIERS[c])}", Modifier.weight(1.45f), compact = true)
                CellText(
                    ColorCode.TOLERANCES[c]?.let { "±${trim(it)}%" } ?: "-",
                    Modifier.weight(1.1f)
                )
                CellText(ColorCode.TCR[c]?.toString() ?: "-", Modifier.weight(1.1f))
            }
        }
        item { SectionTitle("Dung sai theo màu") }
        item {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(40.dp))
                CellText("Tên", Modifier.weight(1.5f), header = true)
                CellText("Giá trị", Modifier.weight(1f), header = true)
            }
        }
        items(ColorCode.TOLERANCES.entries.sortedBy { it.value }) { (c, t) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                Swatch(c.argb)
                CellText(c.label, Modifier.weight(1.5f))
                CellText("±${trim(t)}%", Modifier.weight(1f))
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                Swatch(null)
                CellText("Không dải", Modifier.weight(1.5f))
                CellText("±${trim(ResistorCalculator.IMPLICIT_TOLERANCE_PERCENT)}%", Modifier.weight(1f))
            }
        }
        item { SectionTitle("Hệ số nhiệt độ (dải thứ 6)") }
        item {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(40.dp))
                CellText("Tên", Modifier.weight(1.5f), header = true)
                CellText("ppm/°C", Modifier.weight(1f), header = true)
            }
        }
        items(ColorCode.TCR.entries.sortedBy { it.value }) { (c, p) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                Swatch(c.argb)
                CellText(c.label, Modifier.weight(1.5f))
                CellText(p.toString(), Modifier.weight(1f))
            }
        }
        item { SectionTitle("Bảng EIA-96") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Giá trị gốc (2 chữ số) × hệ số chữ cái:",
                    style = MaterialTheme.typography.bodyMedium
                )
                eia96LegendRows.forEach { chunk ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        chunk.forEach { (letter, mult) ->
                            Text("$letter: ×${trim(mult)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(10.dp),
                Alignment.CenterVertically
            ) {
                repeat(3) {
                    Row(
                        Modifier.weight(1f),
                        Arrangement.spacedBy(4.dp),
                        Alignment.CenterVertically
                    ) {
                        CellText("Mã", Modifier.weight(0.6f), header = true)
                        CellText("Giá trị", Modifier.weight(1f), header = true, align = TextAlign.End)
                    }
                }
            }
        }
        items(
            items = eia96Rows,
            key = { row -> row.first().first },
            contentType = { "eia96-row" }
        ) { row ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                Arrangement.spacedBy(10.dp),
                Alignment.CenterVertically
            ) {
                row.forEach { (code, value) ->
                    Row(
                        Modifier.weight(1f),
                        Arrangement.spacedBy(4.dp),
                        Alignment.CenterVertically
                    ) {
                        CellText(code, Modifier.weight(0.6f))
                        CellText(value, Modifier.weight(1f), align = TextAlign.End)
                    }
                }
            }
        }
    }
}

private fun trim(v: Double?): String {
    if (v == null) return "?"
    val s = if (v >= 1.0 && v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
    return s.replace('.', ',')
}

@Composable
private fun SectionTitle(t: String) {
    Text(t, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun CellText(
    text: String,
    modifier: Modifier = Modifier,
    header: Boolean = false,
    compact: Boolean = false,
    align: TextAlign? = null
) {
    Text(
        text,
        modifier = modifier,
        style = when {
            header -> MaterialTheme.typography.titleSmall
            compact -> MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            else -> MaterialTheme.typography.labelSmall
        },
        fontWeight = if (header) FontWeight.SemiBold else null,
        textAlign = align,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun Swatch(argb: Long?) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        if (argb == null) {
            Modifier
                .size(40.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
        } else {
            Modifier
                .size(40.dp)
                .background(Color(argb), shape)
        }
    )
}
