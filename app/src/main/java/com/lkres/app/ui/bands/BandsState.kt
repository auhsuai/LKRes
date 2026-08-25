package com.lkres.app.ui.bands

import com.lkres.app.core.BandColor
import com.lkres.app.core.CalcResult
import com.lkres.app.core.ResistorCalculator

class BandsState(initialBandCount: Int = 4) {

    var bandCount: Int = initialBandCount
        private set

    var selected: List<BandColor?> = List(initialBandCount) { null }
        private set

    val result: CalcResult?
        get() = if (selected.any { it == null }) null
        else ResistorCalculator.calculate(selected.filterNotNull(), bandCount)

    fun setBandCount(count: Int) {
        bandCount = count
        selected = List(count) { i -> selected.getOrNull(i) }
    }

    fun pick(bandIndex: Int, color: BandColor) {
        selected = selected.toMutableList().also { it[bandIndex] = color }
    }
}
