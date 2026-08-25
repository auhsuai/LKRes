package com.lkres.app.core

import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

data class ColorVariant(val bandCount: Int, val colors: List<BandColor>)

sealed interface EncodingResult {
    data class Encodable(val variants: List<ColorVariant>) : EncodingResult
    data class NotEncodable(val nearestE24: Double) : EncodingResult
}

object ValueToColors {

    private val E24 = listOf(10, 11, 12, 13, 15, 16, 18, 20, 22, 24, 27, 30,
        33, 36, 39, 43, 47, 51, 56, 62, 68, 75, 82, 91)

    // Suy ra từ ColorCode.MULTIPLIERS — một nguồn sự thật duy nhất, không khai trùng lặp.
    private val EXP_TO_COLOR: Map<Int, BandColor> =
        ColorCode.MULTIPLIERS.entries.associate { (color, mult) ->
            log10(mult).roundToInt() to color
        }

    private fun digitColor(d: Int): BandColor =
        BandColor.entries.first { ColorCode.DIGITS[it] == d }

    fun encode(ohms: Double): EncodingResult {
        require(ohms > 0.0) { "ohms phải > 0" }
        val variants = mutableListOf<ColorVariant>()

        val v4 = encodeWithDigits(ohms, 2)
        if (v4 != null) variants += ColorVariant(4, v4)
        val v5 = encodeWithDigits(ohms, 3)
        if (v5 != null) {
            variants += ColorVariant(5, v5)
            variants += ColorVariant(6, v5)
        }
        if (variants.isNotEmpty()) return EncodingResult.Encodable(variants)
        return EncodingResult.NotEncodable(nearestE24(ohms))
    }

    // significand = ohms / 10^exp ; trả colors nếu significand nguyên có đúng `digits` chữ số và exp hợp lệ.
    // Lưu ý: exp PHẢI dùng floor(log10) chứ không phải toInt() (truncation về 0) — nếu không
    // mọi giá trị < 1Ω (log10 âm, VD 0.47 ≈ -0.33) sẽ tính sai số mũ và mất biến thể SILVER.
    private fun encodeWithDigits(ohms: Double, digits: Int): List<BandColor>? {
        val exp = floor(log10(ohms)).roundToInt() - (digits - 1)
        val multColor = EXP_TO_COLOR[exp] ?: return null
        val significand = ohms / 10.0.pow(exp.toDouble())
        val rounded = Math.round(significand * 1e9) / 1e9
        if (Math.abs(rounded - Math.rint(rounded)) > 1e-9) return null
        val s = rounded.toLong()
        if (s < 10.0.pow((digits - 1).toDouble()).toLong() || s >= 10.0.pow(digits.toDouble()).toLong()) return null
        val ds = s.toString().map { it - '0' }
        return ds.map { digitColor(it) } + multColor
    }

    // Gợi ý giá trị E24 KẾ TIẾP (nhỏ nhất trong E24 mà >= ohms).
    // Baseline test yêu cầu nearestE24(1234) == 1300: cả khoảng cách tuyến tính lẫn log-space
    // đều chọn 1200 (12 ∈ E24), nên "gần nhất thuần túy" không thoả bài toán nghiệp vụ;
    // quy ước chọn trở chuẩn cao hơn gần nhất mới khớp baseline.
    private fun nearestE24(ohms: Double): Double {
        val e = floor(log10(ohms)).roundToInt()
        var best = Double.MAX_VALUE
        var bestV = ohms
        for (de in e - 1..e + 1) {
            E24.forEach { base ->
                val v = base * 10.0.pow((de - 1).toDouble())
                if (v >= ohms && v < best) { best = v; bestV = v }
            }
        }
        return bestV
    }
}
