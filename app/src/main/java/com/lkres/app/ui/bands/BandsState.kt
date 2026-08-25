package com.lkres.app.ui.bands

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lkres.app.core.BandColor
import com.lkres.app.core.CalcResult
import com.lkres.app.core.ResistorCalculator

class BandsState(initialBandCount: Int = 4) {

    private var _bandCount by mutableStateOf(initialBandCount)

    val bandCount: Int
        get() = _bandCount

    var selected by mutableStateOf<List<BandColor?>>(List(initialBandCount) { null })
        private set

    val result: CalcResult?
        get() = if (selected.any { it == null }) null
        else ResistorCalculator.calculate(selected.filterNotNull(), bandCount)

    fun setBandCount(count: Int) {
        _bandCount = count
        selected = List(count) { i -> selected.getOrNull(i) }
    }

    fun pick(bandIndex: Int, color: BandColor) {
        selected = selected.toMutableList().also { it[bandIndex] = color }
    }
}
