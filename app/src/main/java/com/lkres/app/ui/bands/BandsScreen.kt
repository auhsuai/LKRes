package com.lkres.app.ui.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import com.lkres.app.core.BandRole
import com.lkres.app.core.CalcResult
import com.lkres.app.core.ColorCode
import com.lkres.app.core.ResistorFormat
import com.lkres.app.core.rolesFor
import com.lkres.app.data.LkResStore
import com.lkres.app.ui.resistor.ResistorCanvas

internal fun chipColor(c: BandColor): Color = Color(c.argb)

@Composable
fun BandsScreen() {
    val state = LkResStore.bands

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        ResistorCanvas(
            bandColors = state.selected,
            activeBandIndex = state.activeBand,
            onBandTap = state::setActiveBand,
            modifier = Modifier.fillMaxWidth().height(140.dp)
        )

        (state.result as? CalcResult.Success)?.let { success ->
            Text(
                ResistorFormat.format(
                    success.resistance.ohms,
                    success.resistance.tolerancePercent,
                    success.resistance.tcrPpmC
                ),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    state.moveActive(-1)
                    LkResStore.persistBands()
                },
                enabled = state.activeBand > 0
            ) { Text("◀") }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(state.bandCount) { i ->
                    val isActive = i == state.activeBand
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                state.setActiveBand(i)
                                LkResStore.persistBands()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${i + 1}",
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            TextButton(
                onClick = {
                    state.moveActive(1)
                    LkResStore.persistBands()
                },
                enabled = state.activeBand < state.bandCount - 1
            ) { Text("▶") }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchSection(onApplyColors = { colors ->
                state.applyColors(colors)
                LkResStore.persistBands()
            })

            when (state.mode) {
                BandsMode.AUTO -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            state.removeBand()
                            LkResStore.persistBands()
                        },
                        enabled = state.canRemoveBand
                    ) { Text("− dải") }
                    OutlinedButton(
                        onClick = {
                            state.addBand()
                            LkResStore.persistBands()
                        },
                        enabled = state.canAddBand
                    ) { Text("+ dải") }
                }
                BandsMode.MANUAL -> SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val segmentCount = BandsState.MAX_BAND_COUNT - BandsState.MIN_BAND_COUNT + 1
                    (BandsState.MIN_BAND_COUNT..BandsState.MAX_BAND_COUNT).forEachIndexed { index, count ->
                        SegmentedButton(
                            selected = state.bandCount == count,
                            onClick = {
                                state.setBandCount(count)
                                LkResStore.persistBands()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = segmentCount)
                        ) { Text("$count dải") }
                    }
                }
            }

            rolesFor(state.bandCount).getOrNull(state.activeBand)?.let { role ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(roleTitle(role), style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        optionsFor(role).forEach { c ->
                            RoleChip(
                                color = c,
                                label = chipLabel(role, c),
                                selected = state.selected.getOrNull(state.activeBand) == c,
                                onClick = {
                                    state.pick(c)
                                    LkResStore.persistBands()
                                }
                            )
                        }
                    }
                }
            }

            when (val result = state.result) {
                is CalcResult.Success -> result.rareWarning?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                is CalcResult.Invalid -> Text(
                    result.reason,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                null -> Unit
            }
        }
    }
}

private fun roleTitle(role: BandRole): String = when (role) {
    BandRole.DIGIT -> "Giá trị"
    BandRole.MULTIPLIER -> "Hệ số nhân"
    BandRole.TOLERANCE -> "Dung sai"
    BandRole.TCR -> "Hệ số nhiệt độ"
}

internal fun optionsFor(role: BandRole): List<BandColor> = when (role) {
    BandRole.DIGIT -> ColorCode.DIGITS.keys.sortedBy { it.ordinal }
    BandRole.MULTIPLIER -> ColorCode.MULTIPLIERS.keys.sortedBy { it.ordinal }
    BandRole.TOLERANCE -> listOf(
        BandColor.BROWN, BandColor.RED, BandColor.GREEN, BandColor.BLUE,
        BandColor.VIOLET, BandColor.GRAY, BandColor.GOLD, BandColor.SILVER
    ).sortedBy { it.ordinal }
    BandRole.TCR -> ColorCode.TCR.keys.sortedBy { it.ordinal }
}

internal fun chipLabel(role: BandRole, c: BandColor): String = when (role) {
    BandRole.DIGIT -> ColorCode.DIGITS.getValue(c).toString()
    BandRole.MULTIPLIER -> multiplierLabel(ColorCode.MULTIPLIERS.getValue(c))
    BandRole.TOLERANCE -> "±${trimNum(ColorCode.TOLERANCES.getValue(c))}%"
    BandRole.TCR -> ColorCode.TCR.getValue(c).toString()
}

private fun trimNum(v: Double): String {
    val rounded = Math.round(v * 100.0) / 100.0
    val s = if (rounded == Math.floor(rounded)) rounded.toLong().toString() else rounded.toString()
    return s.replace('.', ',')
}

private fun multiplierLabel(m: Double): String = when {
    m >= 1e9 -> "×${trimNum(m / 1e9)}G"
    m >= 1e6 -> "×${trimNum(m / 1e6)}M"
    m >= 1e3 -> "×${trimNum(m / 1e3)}k"
    else -> "×${trimNum(m)}"
}

@Composable
internal fun RoleChip(color: BandColor, label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = chipColor(color)
    Box(
        Modifier
            .heightIn(min = 40.dp)
            .widthIn(min = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (bg.luminance() > 0.5f) Color.Black else Color.White,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
