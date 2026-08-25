package com.lkres.app.ui.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import com.lkres.app.core.CalcResult
import com.lkres.app.core.ColorCode
import com.lkres.app.core.ResistorFormat
import com.lkres.app.ui.resistor.ResistorCanvas

private fun chipColor(c: BandColor): Color = Color(c.argb)

@Composable
fun BandsScreen() {
    var state by remember { mutableStateOf(BandsState()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                (3..6).forEachIndexed { index, count ->
                    SegmentedButton(
                        selected = state.bandCount == count,
                        onClick = { state.setBandCount(count) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 4)
                    ) { Text("$count dải") }
                }
            }
        }
        item {
            ResistorCanvas(
                bandColors = state.selected,
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
        }
        state.result?.let { result ->
            item {
                when (result) {
                    is CalcResult.Success -> Column {
                        Text(
                            ResistorFormat.format(
                                result.resistance.ohms,
                                result.resistance.tolerancePercent,
                                result.resistance.tcrPpmC
                            ),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        result.rareWarning?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is CalcResult.Invalid -> Text(result.reason, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        items((0 until state.bandCount).toList(), key = { it }) { bandIndex ->
            BandPickerRow(
                title = "Dải ${bandIndex + 1}",
                options = optionsFor(bandIndex, state.bandCount),
                selected = state.selected[bandIndex],
                onPick = { state.pick(bandIndex, it) }
            )
        }
    }
}

private fun optionsFor(bandIndex: Int, bandCount: Int): List<BandColor> = when {
    bandCount == 6 && bandIndex == 5 -> ColorCode.TCR.keys.sortedBy { it.ordinal }
    bandCount >= 4 && bandIndex == bandCount - 1 -> ColorCode.TOLERANCES.keys.sortedBy { it.ordinal }
    bandIndex < bandCount - (if (bandCount >= 4) 2 else 1) -> ColorCode.DIGITS.keys.sortedBy { it.ordinal }
    else -> ColorCode.MULTIPLIERS.keys.sortedBy { it.ordinal }
}

@Composable
private fun BandPickerRow(
    title: String,
    options: List<BandColor>,
    selected: BandColor?,
    onPick: (BandColor) -> Unit
) {
    Column {
        AssistChip(onClick = {}, label = { Text(title) })
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { c ->
                val isSel = selected == c
                Box(
                    Modifier
                        .size(36.dp)
                        .background(chipColor(c), CircleShape)
                        .border(
                            width = if (isSel) 3.dp else 1.dp,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onPick(c) }
                )
            }
        }
    }
}
