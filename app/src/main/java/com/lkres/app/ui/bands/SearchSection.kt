package com.lkres.app.ui.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import com.lkres.app.core.ColorVariant
import com.lkres.app.core.EncodingResult
import com.lkres.app.core.ResistorFormat
import com.lkres.app.core.ValueParseResult
import com.lkres.app.core.ValueParser
import com.lkres.app.core.ValueToColors
import com.lkres.app.data.LkResStore

private sealed interface SearchUi {
    data object Idle : SearchUi
    data class Failed(val message: String) : SearchUi
    data class SuggestNearest(val nearestOhms: Double) : SearchUi
    data class Results(val ohms: Double, val variants: List<ColorVariant>) : SearchUi
}

private val DEFAULT_TOLERANCE = BandColor.GOLD
private const val SIX_BANDS = 6
private const val FOUR_BANDS = 4
private val CARD_WIDTH = 155.dp
private val DOT_SIZE = 26.dp

@Composable
fun SearchSection(onApplyColors: (List<BandColor?>) -> Unit) {
    var query by remember { mutableStateOf("") }
    var ui by remember { mutableStateOf<SearchUi>(SearchUi.Idle) }

    // Bấm thẻ: áp colors + dung sai GOLD mặc định (giá trị hiện ngay, user chỉnh sau bằng
    // chạm dải trên hình) + TCR null nếu 6 dải.
    fun applySequence(variant: ColorVariant) {
        val tcrPart = if (variant.bandCount == SIX_BANDS) listOf<BandColor?>(null) else emptyList()
        onApplyColors(variant.colors + DEFAULT_TOLERANCE + tcrPart)
    }

    fun defaultVariant(variants: List<ColorVariant>): ColorVariant? =
        variants.firstOrNull { it.bandCount == FOUR_BANDS } ?: variants.firstOrNull()

    // Live search: parse + encode là hàm thuần, rẻ — chỉ chạy khi text thật sự đổi (trong event handler,
    // không phải lúc recomposition) nên không cần memoize thêm.
    fun evaluate(raw: String): SearchUi {
        if (raw.isBlank()) return SearchUi.Idle
        return when (val parsed = ValueParser.parse(raw)) {
            is ValueParseResult.Error -> SearchUi.Failed(parsed.kind.message)
            is ValueParseResult.Success -> when (val enc = ValueToColors.encode(parsed.ohms)) {
                is EncodingResult.Encodable -> SearchUi.Results(parsed.ohms, enc.variants)
                is EncodingResult.NotEncodable -> SearchUi.SuggestNearest(enc.nearestE24)
            }
        }
    }

    // Điểm vào DUY NHẤT cho thay đổi query (gõ phím lẫn bấm chip lịch sử).
    // Encode thành công -> tự áp tổ hợp mặc định (không ghi history); parse lỗi/rỗng -> không đụng màu đã áp.
    fun onQueryChange(newQuery: String) {
        query = newQuery
        val next = evaluate(newQuery)
        ui = next
        if (next is SearchUi.Results) {
            defaultVariant(next.variants)?.let { applySequence(it) }
        }
    }

    fun acceptSuggestion(nearestOhms: Double) {
        when (val enc = ValueToColors.encode(nearestOhms)) {
            is EncodingResult.Encodable -> {
                LkResStore.addRecentSearch(ResistorFormat.format(nearestOhms))
                ui = SearchUi.Results(nearestOhms, enc.variants)
            }
            is EncodingResult.NotEncodable -> Unit
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nhập giá trị: 4700 · 4,7k · 4k7") },
            singleLine = true
        )

        if (LkResStore.historyEnabled && LkResStore.recentSearches.isNotEmpty()) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LkResStore.recentSearches.forEach { item ->
                    SuggestionChip(
                        onClick = { onQueryChange(item) },
                        label = { Text(item) }
                    )
                }
            }
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
                val visibleVariants = if (LkResStore.parallelResults) {
                    current.variants
                } else {
                    listOfNotNull(defaultVariant(current.variants))
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleVariants.forEach { variant ->
                        VariantCard(
                            variant = variant,
                            onSelect = {
                                LkResStore.addRecentSearch(query)
                                applySequence(variant)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VariantCard(variant: ColorVariant, onSelect: () -> Unit) {
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
        }
    }
}
