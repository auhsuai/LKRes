package com.lkres.app.ui.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import com.lkres.app.core.BandRole
import com.lkres.app.core.ColorCode
import com.lkres.app.core.ColorVariant
import com.lkres.app.core.EncodingResult
import com.lkres.app.core.ResistorFormat
import com.lkres.app.core.ValueParseResult
import com.lkres.app.core.ValueParser
import com.lkres.app.core.ValueToColors

private sealed interface SearchUi {
    data object Idle : SearchUi
    data class Failed(val message: String) : SearchUi
    data class SuggestNearest(val nearestOhms: Double) : SearchUi
    data class Results(val ohms: Double, val variants: List<ColorVariant>) : SearchUi
}

private val DEFAULT_TOLERANCE = BandColor.GOLD
private const val SIX_BANDS = 6
private val CARD_WIDTH = 155.dp
private val DOT_SIZE = 26.dp

@Composable
fun SearchSection(onApplyColors: (List<BandColor?>) -> Unit) {
    var query by remember { mutableStateOf("") }
    var ui by remember { mutableStateOf<SearchUi>(SearchUi.Idle) }
    var tolerances by remember { mutableStateOf(emptyMap<Int, BandColor>()) }
    var tcrs by remember { mutableStateOf(emptyMap<Int, BandColor?>()) }

    fun applySequence(variant: ColorVariant, tolColor: BandColor, tcrColor: BandColor?) {
        val tcrPart = if (variant.bandCount == SIX_BANDS) listOf<BandColor?>(tcrColor) else emptyList()
        onApplyColors(variant.colors + tolColor + tcrPart)
    }

    fun showResults(ohms: Double) {
        tolerances = emptyMap()
        tcrs = emptyMap()
        when (val enc = ValueToColors.encode(ohms)) {
            is EncodingResult.Encodable -> ui = SearchUi.Results(ohms, enc.variants)
            is EncodingResult.NotEncodable -> ui = SearchUi.SuggestNearest(enc.nearestE24)
        }
    }

    fun runSearch(raw: String) {
        when (val parsed = ValueParser.parse(raw)) {
            is ValueParseResult.Error -> ui = SearchUi.Failed(parsed.kind.message)
            is ValueParseResult.Success -> showResults(parsed.ohms)
        }
    }

    fun acceptSuggestion(nearestOhms: Double) {
        when (val enc = ValueToColors.encode(nearestOhms)) {
            is EncodingResult.Encodable -> {
                tolerances = emptyMap()
                tcrs = emptyMap()
                ui = SearchUi.Results(nearestOhms, enc.variants)
            }
            is EncodingResult.NotEncodable -> Unit
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                label = { Text("Nhập giá trị: 4700 · 4,7k · 4k7") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { runSearch(query) })
            )
            Button(onClick = { runSearch(query) }) { Text("Tìm") }
        }

        when (val current = ui) {
            SearchUi.Idle -> Unit
            is SearchUi.Failed -> Text(
                current.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            is SearchUi.SuggestNearest -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Không encode được — gợi ý gần nhất: ${ResistorFormat.format(current.nearestOhms)}"
                )
                OutlinedButton(onClick = { acceptSuggestion(current.nearestOhms) }) {
                    Text("Dùng gợi ý")
                }
            }
            is SearchUi.Results -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    ResistorFormat.format(current.ohms),
                    style = MaterialTheme.typography.titleLarge
                )
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    current.variants.forEach { variant ->
                        val count = variant.bandCount
                        val selectedTcr = if (count == SIX_BANDS) tcrs[count] else null
                        VariantCard(
                            variant = variant,
                            tolerance = tolerances[count] ?: DEFAULT_TOLERANCE,
                            tcr = selectedTcr,
                            onSelect = {
                                applySequence(variant, tolerances[count] ?: DEFAULT_TOLERANCE, selectedTcr)
                            },
                            onToleranceChange = { c ->
                                tolerances = tolerances + (count to c)
                                applySequence(variant, c, selectedTcr)
                            },
                            onTcrChange = { c ->
                                tcrs = tcrs + (count to c)
                                applySequence(variant, tolerances[count] ?: DEFAULT_TOLERANCE, c)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VariantCard(
    variant: ColorVariant,
    tolerance: BandColor,
    tcr: BandColor?,
    onSelect: () -> Unit,
    onToleranceChange: (BandColor) -> Unit,
    onTcrChange: (BandColor?) -> Unit
) {
    Card(onClick = onSelect, modifier = Modifier.width(CARD_WIDTH)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${variant.bandCount} dải", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                variant.colors.forEach { c ->
                    Box(
                        Modifier
                            .size(DOT_SIZE)
                            .background(chipColor(c), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
            ChipFlow("Dung sai") {
                optionsFor(BandRole.TOLERANCE).forEach { c ->
                    RoleChip(
                        color = c,
                        label = chipLabel(BandRole.TOLERANCE, c),
                        selected = c == tolerance,
                        onClick = { onToleranceChange(c) }
                    )
                }
            }
            if (variant.bandCount == SIX_BANDS) {
                ChipFlow("TCR (ppm/°C)") {
                    ColorCode.TCR.keys.sortedBy { it.ordinal }.forEach { c ->
                        RoleChip(
                            color = c,
                            label = chipLabel(BandRole.TCR, c),
                            selected = c == tcr,
                            onClick = { onTcrChange(c) }
                        )
                    }
                    EmptyTcrChip(selected = tcr == null, onClick = { onTcrChange(null) })
                }
            }
        }
    }
}

@Composable
private fun ChipFlow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) { content() }
    }
}

@Composable
private fun EmptyTcrChip(selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Trống", style = MaterialTheme.typography.labelMedium)
    }
}
