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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import com.lkres.app.core.ColorVariant
import com.lkres.app.core.ResistorFormat
import com.lkres.app.data.LkResStore

internal sealed interface SearchUi {
    data object Idle : SearchUi
    data class Failed(val message: String) : SearchUi
    data class SuggestNearest(val nearestOhms: Double) : SearchUi
    data class Results(val ohms: Double, val variants: List<ColorVariant>) : SearchUi
}

internal val DEFAULT_TOLERANCE = BandColor.GOLD
internal const val SIX_BANDS = 6
internal const val FOUR_BANDS = 4
private val CARD_WIDTH = 155.dp
private val DOT_SIZE = 26.dp

internal fun defaultVariant(variants: List<ColorVariant>): ColorVariant? =
    variants.firstOrNull { it.bandCount == FOUR_BANDS } ?: variants.firstOrNull()

// Ô nhập search — đặt TRÊN CÙNG tab. State query nằm ở BandsScreen.
@Composable
internal fun SearchInput(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Nhập giá trị cần tìm (4700, 4,7k, 4k7)") },
        singleLine = true
    )
}

// Kết quả search (lỗi parse / gợi ý E24 gần nhất / các thẻ variant).
// Đặt NGAY DƯỚI hàng chip chọn màu, TRƯỚC thanh chuyển dải.
// State ui/dismissed + logic apply nằm ở BandsScreen: bấm thẻ -> onSelectVariant
// (owner tự apply + ẩn thẻ); bấm gợi ý -> onAcceptSuggestion.
@Composable
internal fun SearchResultsSection(
    ui: SearchUi,
    dismissed: Boolean,
    onSelectVariant: (ColorVariant) -> Unit,
    onAcceptSuggestion: (Double) -> Unit
) {
    when (val current = ui) {
        SearchUi.Idle -> Unit
        is SearchUi.Failed -> Text(
            current.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        is SearchUi.SuggestNearest -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Giá trị không thuộc dải chuẩn E24. Gợi ý gần nhất: ${ResistorFormat.format(current.nearestOhms)}"
            )
            OutlinedButton(onClick = { onAcceptSuggestion(current.nearestOhms) }) {
                Text("Dùng giá trị này")
            }
        }
        is SearchUi.Results -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val visibleVariants = if (LkResStore.parallelResults) {
                current.variants
            } else {
                listOfNotNull(defaultVariant(current.variants))
            }
            if (!dismissed) {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleVariants.forEach { variant ->
                        VariantCard(variant = variant, onSelect = { onSelectVariant(variant) })
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
