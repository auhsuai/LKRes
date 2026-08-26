package com.lkres.app.ui.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.lkres.app.core.ColorVariant
import com.lkres.app.core.EncodingResult
import com.lkres.app.core.ResistorFormat
import com.lkres.app.core.ValueParseResult
import com.lkres.app.core.ValueParser
import com.lkres.app.core.ValueToColors
import com.lkres.app.core.rolesFor
import com.lkres.app.data.LkResStore
import com.lkres.app.ui.resistor.ResistorCanvas

internal fun chipColor(c: BandColor): Color = Color(c.argb)

private val BAND_CONTROL_SIZE = 36.dp
private val BAND_INDEX_SIZE = 30.dp
private val BAND_BAR_SPACING = 4.dp

@Composable
fun BandsScreen() {
    val state = LkResStore.bands

    // State search giữ tại đây để tách bố cục: ô nhập TRÊN CÙNG tab,
    // kết quả ngay dưới hàng chip chọn màu (trước thanh chuyển dải).
    var query by remember { mutableStateOf("") }
    var ui by remember { mutableStateOf<SearchUi>(SearchUi.Idle) }
    var dismissed by remember { mutableStateOf(false) }

    // Bấm thẻ: áp colors + dung sai GOLD mặc định (giá trị hiện ngay, user chỉnh sau bằng
    // chạm dải trên hình) + TCR null nếu 6 dải.
    fun applySequence(variant: ColorVariant) {
        val tcrPart = if (variant.bandCount == SIX_BANDS) listOf<BandColor?>(null) else emptyList()
        state.applyColors(variant.colors + DEFAULT_TOLERANCE + tcrPart)
        LkResStore.persistBands()
    }

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

    // Điểm vào DUY NHẤT cho thay đổi query (gõ phím).
    // Encode thành công -> tự áp tổ hợp mặc định; parse lỗi/rỗng -> không đụng màu đã áp.
    // Đang xoá text (độ dài giảm) -> chỉ cập nhật kết quả tìm kiếm,
    // KHÔNG áp lại hình trở (xoá "4700" giữ nguyên 4.7k thay vì nhảy 470->47->4).
    fun onQueryChange(newQuery: String, forceApply: Boolean = false) {
        val isDeleting = !forceApply && newQuery.length < query.length
        dismissed = false
        query = newQuery
        val next = evaluate(newQuery)
        ui = next
        if (!isDeleting && next is SearchUi.Results) {
            defaultVariant(next.variants)?.let { applySequence(it) }
        }
    }

    fun acceptSuggestion(nearestOhms: Double) {
        when (val enc = ValueToColors.encode(nearestOhms)) {
            is EncodingResult.Encodable -> {
                dismissed = false
                ui = SearchUi.Results(nearestOhms, enc.variants)
            }
            is EncodingResult.NotEncodable -> Unit
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        SearchInput(
            query = query,
            onQueryChange = { onQueryChange(it) }
        )

        ResistorCanvas(
            bandColors = state.selected,
            activeBandIndex = state.activeBand,
            onBandTap = state::setActiveBand,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(140.dp)
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

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.mode == BandsMode.MANUAL) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
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

            SearchResultsSection(
                ui = ui,
                dismissed = dismissed,
                onSelectVariant = { variant ->
                    applySequence(variant)
                    dismissed = true
                },
                onAcceptSuggestion = { acceptSuggestion(it) }
            )

            when (val result = state.result) {
                is CalcResult.Invalid -> Text(
                    result.reason,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                is CalcResult.Success, null -> Unit
            }
        }

        BandBar(state)
    }
}

// Thanh chuyển dải cố định ĐÁY màn hình, căn giữa, cuộn ngang khi tràn (@360dp vẫn đủ 6 dải):
// AUTO: [−] ◀ (1)(2)(3)(4)(5)(6) ▶ [+] ; MANUAL: ◀ (1)...(N) ▶ (segmented ở vùng cuộn).
@Composable
private fun BandBar(state: BandsState) {
    val scrollState = rememberScrollState()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .horizontalScroll(scrollState),
        // Nội dung vừa màn hình -> Center. Khi tràn phải về Start vì Center + horizontalScroll
        // cắt mất mép trái (phần âm offset không cuộn tới được).
        horizontalArrangement = if (scrollState.maxValue == 0) Arrangement.Center else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BAND_BAR_SPACING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (state.mode) {
                BandsMode.AUTO -> {
                    BandAdjustButton(
                        label = "−",
                        enabled = state.canRemoveBand,
                        onClick = {
                            state.removeBand()
                            LkResStore.persistBands()
                        }
                    )
                    BandNav(state)
                    BandAdjustButton(
                        label = "+",
                        enabled = state.canAddBand,
                        onClick = {
                            state.addBand()
                            LkResStore.persistBands()
                        }
                    )
                }
                BandsMode.MANUAL -> BandNav(state)
            }
        }
    }
}

// ◀ + hàng ô chỉ mục + ▶ dùng chung cho 2 mode.
@Composable
private fun BandNav(state: BandsState) {
    BandAdjustButton(
        label = "◀",
        enabled = state.activeBand > 0,
        onClick = {
            state.moveActive(-1)
            LkResStore.persistBands()
        }
    )

    Row(horizontalArrangement = Arrangement.spacedBy(BAND_BAR_SPACING)) {
        repeat(state.bandCount) { i ->
            BandIndexBox(state = state, index = i)
        }
    }

    BandAdjustButton(
        label = "▶",
        enabled = state.activeBand < state.bandCount - 1,
        onClick = {
            state.moveActive(1)
            LkResStore.persistBands()
        }
    )
}

@Composable
private fun BandIndexBox(state: BandsState, index: Int) {
    val isActive = index == state.activeBand
    Box(
        Modifier
            .size(BAND_INDEX_SIZE)
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
                state.setActiveBand(index)
                LkResStore.persistBands()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${index + 1}",
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun BandAdjustButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(BAND_CONTROL_SIZE)
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            color = if (enabled) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
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
