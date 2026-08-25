package com.lkres.app.core

object ResistorFormat {

    fun format(ohms: Double, tolerancePercent: Double? = null, tcrPpmC: Int? = null): String {
        val (value, unit) = when {
            ohms >= 1e9 -> ohms / 1e9 to "GΩ"
            ohms >= 1e6 -> ohms / 1e6 to "MΩ"
            ohms >= 1e3 -> ohms / 1e3 to "kΩ"
            else -> ohms to "Ω"
        }
        val sb = StringBuilder(formatNumber(value)).append(' ').append(unit)
        tolerancePercent?.let { sb.append(" ±").append(formatNumber(it)).append('%') }
        tcrPpmC?.let { sb.append("  ").append(it).append(" ppm/°C") }
        return sb.toString()
    }

    private fun formatNumber(v: Double): String {
        val rounded = Math.round(v * 100.0) / 100.0
        return if (rounded == Math.floor(rounded)) rounded.toLong().toString() else rounded.toString()
    }
}
