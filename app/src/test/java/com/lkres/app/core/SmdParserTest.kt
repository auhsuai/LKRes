package com.lkres.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmdParserTest {

    private fun ohmsOf(code: String): Double {
        val r = SmdParser.parse(code)
        assertTrue("$code phải Success, thực tế $r", r is SmdResult.Success)
        return (r as SmdResult.Success).resistance.ohms
    }

    private fun errorOf(code: String): SmdErrorKind {
        val r = SmdParser.parse(code)
        assertTrue("$code phải Error, thực tế $r", r is SmdResult.Error)
        return (r as SmdResult.Error).kind
    }

    @Test
    fun `3 số 472 = 4_7kOhm`() {
        assertEquals(4700.0, ohmsOf("472"), 1e-9)
    }

    @Test
    fun `4 số 4702 = 47kOhm`() {
        assertEquals(47000.0, ohmsOf("4702"), 1e-9)
    }

    @Test
    fun `R ở giữa 4R7 = 4_7 Ohm`() {
        assertEquals(4.7, ohmsOf("4R7"), 1e-9)
    }

    @Test
    fun `R đầu R47 = 0_47 Ohm`() {
        assertEquals(0.47, ohmsOf("R47"), 1e-9)
    }

    @Test
    fun `0R22 = 0_22 Ohm`() {
        assertEquals(0.22, ohmsOf("0R22"), 1e-9)
    }

    @Test
    fun `0 đơn lẻ là jumper 0 Ohm`() {
        assertEquals(0.0, ohmsOf("0"), 1e-9)
    }

    @Test
    fun `EIA96 01C = 10kOhm`() {
        assertEquals(10000.0, ohmsOf("01C"), 1e-9)
    }

    @Test
    fun `EIA96 96Z = 0_976 Ohm`() {
        assertEquals(0.976, ohmsOf("96Z"), 1e-9)
    }

    @Test
    fun `chữ thường tự uppercase`() {
        assertEquals(ohmsOf("4R7"), ohmsOf("4r7"), 1e-9)
        assertEquals(ohmsOf("01C"), ohmsOf("01c"), 1e-9)
    }

    @Test
    fun `chuỗi rỗng = EMPTY`() {
        assertEquals(SmdErrorKind.EMPTY, errorOf("   "))
    }

    @Test
    fun `ký tự lạ = INVALID_CHARACTERS`() {
        assertEquals(SmdErrorKind.INVALID_CHARACTERS, errorOf("4a2"))
        assertEquals(SmdErrorKind.INVALID_CHARACTERS, errorOf("01W"))
    }

    @Test
    fun `2 số thuần và 5 số = UNRECOGNIZED_LENGTH`() {
        assertEquals(SmdErrorKind.UNRECOGNIZED_LENGTH, errorOf("47"))
        assertEquals(SmdErrorKind.UNRECOGNIZED_LENGTH, errorOf("47234"))
    }

    @Test
    fun `cặp số 99 không có trong EIA96 = EIA96_NOT_FOUND`() {
        assertEquals(SmdErrorKind.EIA96_NOT_FOUND, errorOf("99C"))
    }
}
