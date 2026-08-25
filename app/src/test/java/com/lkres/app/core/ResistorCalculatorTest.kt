package com.lkres.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResistorCalculatorTest {

    private fun calc(vararg c: BandColor) = ResistorCalculator.calculate(c.toList(), c.size)

    @Test
    fun `4 dải vàng-tím-đỏ-vàng kim = 4_7kOhm ±5%`() {
        val r = calc(BandColor.YELLOW, BandColor.VIOLET, BandColor.RED, BandColor.GOLD)
        assertTrue(r is CalcResult.Success)
        r as CalcResult.Success
        assertEquals(4700.0, r.resistance.ohms, 1e-9)
        assertEquals(5.0, r.resistance.tolerancePercent!!, 1e-9)
        assertNull(r.resistance.tcrPpmC)
    }

    @Test
    fun `3 dải nâu-đen-cam = 10kOhm dung sai ngầm định ±20%`() {
        val r = calc(BandColor.BROWN, BandColor.BLACK, BandColor.ORANGE)
        assertTrue(r is CalcResult.Success)
        r as CalcResult.Success
        assertEquals(10000.0, r.resistance.ohms, 1e-9)
        assertEquals(20.0, r.resistance.tolerancePercent!!, 1e-9)
    }

    @Test
    fun `5 dải nâu-đen-đen-đỏ-nâu = 10kOhm ±1%`() {
        val r = calc(BandColor.BROWN, BandColor.BLACK, BandColor.BLACK, BandColor.RED, BandColor.BROWN)
        assertTrue(r is CalcResult.Success)
        r as CalcResult.Success
        assertEquals(10000.0, r.resistance.ohms, 1e-9)
        assertEquals(1.0, r.resistance.tolerancePercent!!, 1e-9)
    }

    @Test
    fun `6 dải có TCR`() {
        val r = calc(BandColor.BROWN, BandColor.BLACK, BandColor.BLACK, BandColor.RED, BandColor.BROWN, BandColor.RED)
        assertTrue(r is CalcResult.Success)
        r as CalcResult.Success
        assertEquals(10000.0, r.resistance.ohms, 1e-9)
        assertEquals(50, r.resistance.tcrPpmC)
    }

    @Test
    fun `multiplier bạc cho giá trị dưới 1 Ohm`() {
        val r = calc(BandColor.BROWN, BandColor.BLACK, BandColor.SILVER, BandColor.GOLD)
        assertTrue(r is CalcResult.Success)
        r as CalcResult.Success
        assertEquals(0.1, r.resistance.ohms, 1e-9)
    }

    @Test
    fun `cảnh báo khi digit đầu là đen`() {
        val r = calc(BandColor.BLACK, BandColor.BLACK, BandColor.BROWN, BandColor.GOLD)
        assertTrue(r is CalcResult.Success)
        r as CalcResult.Success
        assertTrue(r.rareWarning != null)
    }

    @Test
    fun `dải dung sai chọn trắng là Invalid`() {
        val r = calc(BandColor.YELLOW, BandColor.VIOLET, BandColor.RED, BandColor.WHITE)
        assertTrue(r is CalcResult.Invalid)
    }

    @Test
    fun `dải TCR chọn đen là Invalid`() {
        val r = calc(BandColor.BROWN, BandColor.BLACK, BandColor.BLACK, BandColor.RED, BandColor.BROWN, BandColor.BLACK)
        assertTrue(r is CalcResult.Invalid)
    }

    @Test
    fun `số màu không khớp số dải là Invalid`() {
        val r = ResistorCalculator.calculate(listOf(BandColor.BROWN, BandColor.BLACK), 4)
        assertTrue(r is CalcResult.Invalid)
    }
}
