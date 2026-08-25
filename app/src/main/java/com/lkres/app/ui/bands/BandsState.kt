package com.lkres.app.ui.bands

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lkres.app.core.BandColor
import com.lkres.app.core.BandRole
import com.lkres.app.core.CalcResult
import com.lkres.app.core.ColorCode
import com.lkres.app.core.ResistorCalculator
import com.lkres.app.core.rolesFor

enum class BandsMode { AUTO, MANUAL }

class BandsState {

    private var _mode by mutableStateOf(BandsMode.AUTO)
    private var _bandCount by mutableStateOf(MIN_BAND_COUNT)
    private var _selected by mutableStateOf<List<BandColor?>>(List(MIN_BAND_COUNT) { null })
    private var _activeBand by mutableStateOf(0)

    // Màu của dải bị bỏ khi removeBand — khôi phục khi addBand lại cùng slot.
    private val rememberedColors = mutableMapOf<Int, BandColor>()

    val mode: BandsMode
        get() = _mode

    val bandCount: Int
        get() = _bandCount

    val selected: List<BandColor?>
        get() = _selected

    val activeBand: Int
        get() = _activeBand

    val canAddBand: Boolean
        get() = _bandCount < MAX_BAND_COUNT

    val canRemoveBand: Boolean
        get() = _bandCount > MIN_BAND_COUNT

    val result: CalcResult?
        get() {
            if (_selected.any { it == null }) return null
            return ResistorCalculator.calculate(_selected.filterNotNull(), _bandCount)
        }

    fun setMode(m: BandsMode) {
        _mode = m
    }

    fun setBandCount(n: Int) {
        resize(n.coerceIn(MIN_BAND_COUNT, MAX_BAND_COUNT))
    }

    fun addBand() {
        if (!canAddBand) return
        val newIndex = _bandCount
        resize(_bandCount + 1)
        val restored = rememberedColors.remove(newIndex)
        if (restored != null) {
            assignAt(newIndex, restored)
        }
    }

    fun removeBand() {
        if (!canRemoveBand) return
        val droppedIndex = _bandCount - 1
        val dropped = _selected.getOrNull(droppedIndex)
        if (dropped != null) {
            rememberedColors[droppedIndex] = dropped
        }
        resize(droppedIndex)
    }

    fun setActiveBand(i: Int) {
        _activeBand = i.coerceIn(0, _bandCount - 1)
    }

    fun moveActive(delta: Int) {
        setActiveBand(_activeBand + delta)
    }

    fun pick(color: BandColor) {
        val role = rolesFor(_bandCount).getOrNull(_activeBand) ?: return
        val allowed = when (role) {
            BandRole.DIGIT -> ColorCode.DIGITS
            BandRole.MULTIPLIER -> ColorCode.MULTIPLIERS
            BandRole.TOLERANCE -> ColorCode.TOLERANCES
            BandRole.TCR -> ColorCode.TCR
        }
        if (!allowed.containsKey(color)) return
        assignAt(_activeBand, color)
    }

    fun applyColors(colors: List<BandColor?>) {
        rememberedColors.clear()
        _bandCount = colors.size
        _selected = colors.toList()
        _activeBand = 0
    }

    fun pick(bandIndex: Int, color: BandColor) {
        assignAt(bandIndex, color)
    }

    private fun resize(newCount: Int) {
        _bandCount = newCount
        _selected = List(newCount) { i -> _selected.getOrNull(i) }
        _activeBand = _activeBand.coerceIn(0, newCount - 1)
    }

    private fun assignAt(index: Int, color: BandColor) {
        if (index !in _selected.indices) return
        _selected = _selected.toMutableList().also { it[index] = color }
    }

    companion object {
        const val MIN_BAND_COUNT = 3
        const val MAX_BAND_COUNT = 6
    }
}
