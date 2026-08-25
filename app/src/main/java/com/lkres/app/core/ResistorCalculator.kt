package com.lkres.app.core

data class Resistance(
    val ohms: Double,
    val tolerancePercent: Double?,
    val tcrPpmC: Int? = null
)

sealed interface CalcResult {
    data class Success(val resistance: Resistance, val rareWarning: String?) : CalcResult
    data class Invalid(val reason: String) : CalcResult
}

object ResistorCalculator {

    const val IMPLICIT_TOLERANCE_PERCENT = 20.0

    fun calculate(colors: List<BandColor>, bandCount: Int): CalcResult {
        if (bandCount !in 3..6) return CalcResult.Invalid("Số dải phải từ 3 đến 6")
        if (colors.size != bandCount) return CalcResult.Invalid("Số màu chọn không khớp số dải")

        val digitCount = if (bandCount <= 4) 2 else 3
        val multIndex = digitCount
        val tolIndex = multIndex + 1
        val tcrIndex = tolIndex + 1

        for (i in 0 until digitCount) {
            if (colors[i] !in ColorCode.DIGITS) {
                return CalcResult.Invalid("Dải ${i + 1} không phải màu digit hợp lệ")
            }
        }
        if (colors[multIndex] !in ColorCode.MULTIPLIERS) {
            return CalcResult.Invalid("Dải nhân không hợp lệ")
        }

        val significand = colors.take(digitCount)
            .map { ColorCode.DIGITS.getValue(it) }
            .fold(0) { acc, d -> acc * 10 + d }
        val ohms = significand * ColorCode.MULTIPLIERS.getValue(colors[multIndex])

        val tolerance: Double = when {
            bandCount >= 4 -> {
                val t = colors[tolIndex]
                ColorCode.TOLERANCES[t] ?: return CalcResult.Invalid("Dải dung sai không hợp lệ")
            }
            else -> IMPLICIT_TOLERANCE_PERCENT
        }

        val tcr: Int? = if (bandCount == 6) {
            val t = colors[tcrIndex]
            ColorCode.TCR[t] ?: return CalcResult.Invalid("Dải hệ số nhiệt không hợp lệ")
        } else null

        val warning = when (colors.first()) {
            BandColor.BLACK -> "Digit đầu là đen (0) — hiếm gặp trên điện trở thật"
            BandColor.WHITE -> "Digit đầu là trắng (9) — hiếm gặp trên điện trở thật"
            else -> null
        }
        return CalcResult.Success(Resistance(ohms, tolerance, tcr), warning)
    }
}
