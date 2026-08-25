package com.lkres.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ResistorFormatTest {

    @Test
    fun `4700 ohm ±5% = 4_7 kOhm ±5%`() {
        assertEquals("4.7 kΩ ±5%", ResistorFormat.format(4700.0, 5.0))
    }

    @Test
    fun `470 ohm không đơn vị lớn`() {
        assertEquals("470 Ω", ResistorFormat.format(470.0))
    }

    @Test
    fun `1 triệu ohm = 1 MOhm`() {
        assertEquals("1 MΩ", ResistorFormat.format(1e6))
    }

    @Test
    fun `1 tỷ ohm = 1 GOhm`() {
        assertEquals("1 GΩ", ResistorFormat.format(1e9))
    }

    @Test
    fun `dưới 1 ohm giữ số thập phân`() {
        assertEquals("0.47 Ω", ResistorFormat.format(0.47))
    }

    @Test
    fun `biên 999 và 1000`() {
        assertEquals("999 Ω", ResistorFormat.format(999.0))
        assertEquals("1 kΩ", ResistorFormat.format(1000.0))
    }

    @Test
    fun `có TCR`() {
        assertEquals("10 kΩ ±1%  100 ppm/°C", ResistorFormat.format(10000.0, 1.0, 100))
    }

    @Test
    fun `số lẻ làm tròn 2 chữ số`() {
        assertEquals("33.33 Ω", ResistorFormat.format(100.0 / 3.0))
    }
}
