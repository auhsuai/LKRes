package com.lkres.app.core

enum class BandRole { DIGIT, MULTIPLIER, TOLERANCE, TCR }

fun rolesFor(bandCount: Int): List<BandRole> = when (bandCount) {
    3 -> listOf(BandRole.DIGIT, BandRole.DIGIT, BandRole.MULTIPLIER)
    4 -> listOf(BandRole.DIGIT, BandRole.DIGIT, BandRole.MULTIPLIER, BandRole.TOLERANCE)
    5 -> listOf(BandRole.DIGIT, BandRole.DIGIT, BandRole.DIGIT, BandRole.MULTIPLIER, BandRole.TOLERANCE)
    6 -> listOf(BandRole.DIGIT, BandRole.DIGIT, BandRole.DIGIT, BandRole.MULTIPLIER, BandRole.TOLERANCE, BandRole.TCR)
    else -> emptyList()
}
