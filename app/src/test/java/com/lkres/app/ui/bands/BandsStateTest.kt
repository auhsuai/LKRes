package com.lkres.app.ui.bands

import com.lkres.app.core.BandColor
import com.lkres.app.core.CalcResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BandsStateTest {

    @Test
    fun `mặc định 4 dải chưa chọn gì thì result null`() {
        val s = BandsState()
        assertEquals(4, s.bandCount)
        assertNull(s.result)
    }

    @Test
    fun `chọn đủ màu 4 dải ra 4_7kOhm`() {
        val s = BandsState()
        s.pick(0, BandColor.YELLOW)
        s.pick(1, BandColor.VIOLET)
        s.pick(2, BandColor.RED)
        s.pick(3, BandColor.GOLD)
        val r = s.result
        assertTrue(r is CalcResult.Success)
        assertEquals(4700.0, (r as CalcResult.Success).resistance.ohms, 1e-9)
    }

    @Test
    fun `đổi 4 sang 5 dải giữ màu cũ và thêm slot null`() {
        val s = BandsState()
        s.pick(0, BandColor.BROWN)
        s.pick(1, BandColor.BLACK)
        s.pick(2, BandColor.BLACK)
        s.pick(3, BandColor.RED)
        s.setBandCount(5)
        assertEquals(listOf<BandColor?>(BandColor.BROWN, BandColor.BLACK, BandColor.BLACK, BandColor.RED, null), s.selected)
        assertNull(s.result)
    }

    @Test
    fun `đổi 6 về 4 dải cắt bớt màu thừa`() {
        val s = BandsState(6)
        s.setBandCount(4)
        assertEquals(4, s.selected.size)
    }

    @Test
    fun `chọn lại màu cùng vị trí ghi đè`() {
        val s = BandsState()
        s.pick(0, BandColor.YELLOW)
        s.pick(0, BandColor.BROWN)
        assertEquals(BandColor.BROWN, s.selected[0])
    }
}
