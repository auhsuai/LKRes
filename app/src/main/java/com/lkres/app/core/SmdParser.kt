package com.lkres.app.core

import kotlin.math.pow

sealed interface SmdResult {
    data class Success(val resistance: Resistance) : SmdResult
    data class Error(val kind: SmdErrorKind) : SmdResult
}

enum class SmdErrorKind(val message: String) {
    EMPTY("Nhập mã trở dán cần tra"),
    INVALID_CHARACTERS("Mã chỉ gồm chữ số, chữ R hoặc mã EIA-96"),
    UNRECOGNIZED_LENGTH("Mã không đúng định dạng chuẩn (3 chữ số, 4 chữ số, 4R7 hoặc EIA-96)"),
    EIA96_NOT_FOUND("Mã EIA-96 không tồn tại trong bảng chuẩn")
}

object SmdParser {

    fun parse(raw: String): SmdResult {
        val code = raw.trim().uppercase()
        if (code.isEmpty()) return SmdResult.Error(SmdErrorKind.EMPTY)
        if (code == "0") return SmdResult.Success(Resistance(0.0, null))

        val isNumericR = code.all { it in '0'..'9' || it == 'R' }
        val isEia96Shape = code.length == 3 &&
                code[0] in '0'..'9' && code[1] in '0'..'9' &&
                code[2] in Eia96.MULTIPLIERS.keys
        if (!isNumericR && !isEia96Shape) {
            return SmdResult.Error(SmdErrorKind.INVALID_CHARACTERS)
        }

        if ('R' in code) return parseRNotation(code)

        return when {
            code.length == 3 && code[2] !in Eia96.MULTIPLIERS.keys -> parse3Digit(code)
            code.length == 4 -> parse4Digit(code)
            code.length == 3 -> parseEia96(code)
            else -> SmdResult.Error(SmdErrorKind.UNRECOGNIZED_LENGTH)
        }
    }

    private fun parseRNotation(code: String): SmdResult {
        val parts = code.split('R', limit = 2)
        val intPart = parts[0]
        val fracPart = parts.getOrElse(1) { "" }
        if (intPart.isEmpty() && fracPart.isEmpty()) {
            return SmdResult.Error(SmdErrorKind.INVALID_CHARACTERS)
        }
        if (intPart.length > 3 || fracPart.length > 2 ||
            (intPart.isNotEmpty() && intPart.any { it !in '0'..'9' }) ||
            fracPart.any { it !in '0'..'9' }
        ) {
            return SmdResult.Error(SmdErrorKind.INVALID_CHARACTERS)
        }
        val value = (intPart.ifEmpty { "0" } + "." + fracPart.ifEmpty { "0" }).toDouble()
        return SmdResult.Success(Resistance(value, null))
    }

    private fun parse3Digit(code: String): SmdResult {
        val significand = code.substring(0, 2).toDouble()
        val ohms = significand * 10.0.pow(code[2] - '0')
        return SmdResult.Success(Resistance(ohms, null))
    }

    private fun parse4Digit(code: String): SmdResult {
        val significand = code.substring(0, 3).toDouble()
        val ohms = significand * 10.0.pow(code[3] - '0')
        return SmdResult.Success(Resistance(ohms, null))
    }

    private fun parseEia96(code: String): SmdResult {
        val base = Eia96.VALUES[code.substring(0, 2)]
        val mult = Eia96.MULTIPLIERS[code[2]]
        if (base == null || mult == null) return SmdResult.Error(SmdErrorKind.EIA96_NOT_FOUND)
        return SmdResult.Success(Resistance(base * mult, null))
    }
}
