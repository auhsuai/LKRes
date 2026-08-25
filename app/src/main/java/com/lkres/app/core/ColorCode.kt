package com.lkres.app.core

enum class BandColor(val label: String, val argb: Long) {
    BLACK("Đen", 0xFF111111),
    BROWN("Nâu", 0xFF7B4A21),
    RED("Đỏ", 0xFFD32F2F),
    ORANGE("Cam", 0xFFF57C00),
    YELLOW("Vàng", 0xFFFBC02D),
    GREEN("Lục", 0xFF388E3C),
    BLUE("Lam", 0xFF1976D2),
    VIOLET("Tím", 0xFF7B1FA2),
    GRAY("Xám", 0xFF9E9E9E),
    WHITE("Trắng", 0xFFF5F5F5),
    GOLD("Vàng kim", 0xFFC9A227),
    SILVER("Bạc", 0xFFB8BCC2)
}

object ColorCode {
    val DIGITS: Map<BandColor, Int> = mapOf(
        BandColor.BLACK to 0, BandColor.BROWN to 1, BandColor.RED to 2,
        BandColor.ORANGE to 3, BandColor.YELLOW to 4, BandColor.GREEN to 5,
        BandColor.BLUE to 6, BandColor.VIOLET to 7, BandColor.GRAY to 8,
        BandColor.WHITE to 9
    )

    val MULTIPLIERS: Map<BandColor, Double> = mapOf(
        BandColor.BLACK to 1e0, BandColor.BROWN to 1e1, BandColor.RED to 1e2,
        BandColor.ORANGE to 1e3, BandColor.YELLOW to 1e4, BandColor.GREEN to 1e5,
        BandColor.BLUE to 1e6, BandColor.VIOLET to 1e7, BandColor.GRAY to 1e8,
        BandColor.WHITE to 1e9, BandColor.GOLD to 1e-1, BandColor.SILVER to 1e-2
    )

    val TOLERANCES: Map<BandColor, Double> = mapOf(
        BandColor.BROWN to 1.0, BandColor.RED to 2.0, BandColor.ORANGE to 0.05,
        BandColor.YELLOW to 0.02, BandColor.GREEN to 0.5, BandColor.BLUE to 0.25,
        BandColor.VIOLET to 0.1, BandColor.GRAY to 0.01,
        BandColor.GOLD to 5.0, BandColor.SILVER to 10.0
    )

    val TCR: Map<BandColor, Int> = mapOf(
        BandColor.BROWN to 100, BandColor.RED to 50, BandColor.ORANGE to 15,
        BandColor.YELLOW to 25, BandColor.GREEN to 20, BandColor.BLUE to 10,
        BandColor.VIOLET to 5, BandColor.GRAY to 1
    )
}
