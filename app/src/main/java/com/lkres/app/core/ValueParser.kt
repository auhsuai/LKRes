package com.lkres.app.core

sealed interface ValueParseResult {
    data class Success(val ohms: Double) : ValueParseResult
    data class Error(val kind: ValueErrorKind) : ValueParseResult
}

enum class ValueErrorKind(val message: String) {
    EMPTY("Nhập giá trị cần tìm"),
    INVALID_FORMAT("VD hợp lệ: 4700 · 4,7k · 4.7k · 4k7 · 1M"),
    NON_POSITIVE("Giá trị phải lớn hơn 0"),
    SMD_STYLE("Mã dạng 4R7 là mã SMD — hãy tra ở tab SMD")
}

object ValueParser {

    private val SMD_R = Regex("""^\d*R\d+""")

    fun parse(raw: String): ValueParseResult {
        val s = raw.trim().uppercase()
            .replace("Ω", "").replace("OHM", "").replace(" ", "")
        if (s.isEmpty()) return ValueParseResult.Error(ValueErrorKind.EMPTY)
        if ('R' in s && SMD_R.matches(s)) return ValueParseResult.Error(ValueErrorKind.SMD_STYLE)

        // Form 1: [digits][.,digits][K|M|G]   — 4700 / 4,7k / 1k
        val formA = Regex("""^(\d+)(?:[.,](\d+))?([KMG]?)$""").find(s)
        // Form 2: [digits][K|M|G][digits]     — 4k7 / 1k5
        val formB = Regex("""^(\d+)([KMG])(\d+)$""").find(s)

        val value: Double = when {
            formA != null -> {
                val g = formA.groupValues
                base(g[1], g[2]) * multiplierOf(g[3].firstOrNull())
            }
            formB != null -> {
                val (intPart, mult, fracPart) = formB.destructured
                base(intPart, fracPart) * multiplierOf(mult.first())
            }
            else -> return ValueParseResult.Error(ValueErrorKind.INVALID_FORMAT)
        }
        if (value <= 0.0) return ValueParseResult.Error(ValueErrorKind.NON_POSITIVE)
        return ValueParseResult.Success(value)
    }

    private fun base(intPart: String, fracPart: String): Double =
        (intPart + "." + fracPart.ifEmpty { "0" }).toDouble()

    private fun multiplierOf(c: Char?): Double = when (c) {
        'K' -> 1e3
        'M' -> 1e6
        'G' -> 1e9
        else -> 1.0
    }
}
