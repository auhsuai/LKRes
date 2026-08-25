package com.lkres.app.ui.bands

import com.lkres.app.core.BandColor
import com.lkres.app.core.BandRole
import com.lkres.app.core.CalcResult
import com.lkres.app.core.rolesFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BandsStateTest {

    // ---- AUTO mặc định ----

    @Test
    fun `auto mặc định 3 dải chưa chọn gì thì result null`() {
        val s = BandsState()
        assertEquals(BandsMode.AUTO, s.mode)
        assertEquals(3, s.bandCount)
        assertEquals(listOf<BandColor?>(null, null, null), s.selected)
        assertEquals(0, s.activeBand)
        assertTrue(s.canAddBand)
        assertFalse(s.canRemoveBand)
        assertNull(s.result)
    }

    @Test
    fun `addBand từ 3 lên 4 giữ màu cũ và slot mới rỗng`() {
        val s = BandsState()
        s.pick(BandColor.BROWN)
        assertTrue(s.canAddBand)
        s.addBand()
        assertEquals(4, s.bandCount)
        assertEquals(listOf<BandColor?>(BandColor.BROWN, null, null, null), s.selected)
    }

    @Test
    fun `removeBand từ 4 về 3 nhớ màu dải bị bỏ`() {
        val s = BandsState()
        s.addBand()
        s.setActiveBand(3)
        s.pick(BandColor.GOLD)
        assertTrue(s.canRemoveBand)
        s.removeBand()
        assertEquals(3, s.bandCount)
        assertFalse(s.selected.contains(BandColor.GOLD))
    }

    @Test
    fun `removeBand rồi addBand lại khôi phục màu đã nhớ`() {
        val s = BandsState()
        s.addBand()
        s.setActiveBand(3)
        s.pick(BandColor.GOLD)
        s.removeBand()
        assertEquals(3, s.bandCount)
        s.addBand()
        assertEquals(4, s.bandCount)
        assertEquals(BandColor.GOLD, s.selected[3])
    }

    // ---- Biên số dải ----

    @Test
    fun `đủ 6 dải thì canAddBand false và addBand không tác dụng`() {
        val s = BandsState()
        repeat(3) { s.addBand() }
        assertEquals(6, s.bandCount)
        assertFalse(s.canAddBand)
        val trước = s.selected
        s.addBand()
        assertEquals(6, s.bandCount)
        assertEquals(trước, s.selected)
    }

    @Test
    fun `còn 3 dải thì canRemoveBand false và removeBand không tác dụng`() {
        val s = BandsState()
        assertFalse(s.canRemoveBand)
        s.removeBand()
        assertEquals(3, s.bandCount)
    }

    // ---- pick theo vai + activeBand ----

    @Test
    fun `pick gán màu cho activeBand và không tự nhảy dải`() {
        val s = BandsState()
        s.pick(BandColor.BROWN)
        assertEquals(BandColor.BROWN, s.selected[0])
        assertEquals(0, s.activeBand)
        s.moveActive(1)
        s.pick(BandColor.BLACK)
        assertEquals(BandColor.BLACK, s.selected[1])
        assertEquals(BandColor.BROWN, s.selected[0])
        assertEquals(1, s.activeBand)
    }

    @Test
    fun `pick sai vai thì no-op`() {
        val s = BandsState()
        s.pick(BandColor.GOLD)
        assertNull(s.selected[0])
    }

    @Test
    fun `moveActive kẹp biên trái và phải`() {
        val s = BandsState()
        s.moveActive(-1)
        assertEquals(0, s.activeBand)
        s.setActiveBand(s.bandCount - 1)
        s.moveActive(1)
        assertEquals(s.bandCount - 1, s.activeBand)
    }

    // ---- applyColors (Search) ----

    @Test
    fun `applyColors bốn dải đổi bandCount selected và activeBand về 0`() {
        val s = BandsState()
        s.setActiveBand(2)
        s.applyColors(
            listOf(
                BandColor.BROWN,
                BandColor.BLACK,
                BandColor.RED,
                BandColor.GOLD
            )
        )
        assertEquals(4, s.bandCount)
        assertEquals(
            listOf<BandColor?>(
                BandColor.BROWN,
                BandColor.BLACK,
                BandColor.RED,
                BandColor.GOLD
            ),
            s.selected
        )
        assertEquals(0, s.activeBand)
        assertTrue(s.result is CalcResult.Success)
    }

    // ---- Mode & setBandCount ----

    @Test
    fun `setMode giữ nguyên màu và số dải`() {
        val s = BandsState()
        s.pick(BandColor.BROWN)
        s.setMode(BandsMode.MANUAL)
        assertEquals(BandsMode.MANUAL, s.mode)
        assertEquals(BandColor.BROWN, s.selected[0])
        assertEquals(3, s.bandCount)
        s.setMode(BandsMode.AUTO)
        assertEquals(BandsMode.AUTO, s.mode)
        assertEquals(BandColor.BROWN, s.selected[0])
    }

    @Test
    fun `setBandCount manual 3 lên 5 giữ màu cũ`() {
        val s = BandsState()
        s.setMode(BandsMode.MANUAL)
        s.pick(BandColor.BROWN)
        s.setBandCount(5)
        assertEquals(5, s.bandCount)
        assertEquals(
            listOf<BandColor?>(BandColor.BROWN, null, null, null, null),
            s.selected
        )
    }

    @Test
    fun `setBandCount kẹp trong khoảng 3 đến 6`() {
        val s = BandsState()
        s.setBandCount(10)
        assertEquals(6, s.bandCount)
        s.setBandCount(1)
        assertEquals(3, s.bandCount)
    }

    // ---- rolesFor (core, dùng chung quy tắc vai) ----

    @Test
    fun `rolesFor đúng vai cho 4 trường hợp số dải`() {
        assertEquals(
            listOf(BandRole.DIGIT, BandRole.DIGIT, BandRole.MULTIPLIER),
            rolesFor(3)
        )
        assertEquals(
            listOf(
                BandRole.DIGIT,
                BandRole.DIGIT,
                BandRole.MULTIPLIER,
                BandRole.TOLERANCE
            ),
            rolesFor(4)
        )
        assertEquals(
            listOf(
                BandRole.DIGIT,
                BandRole.DIGIT,
                BandRole.DIGIT,
                BandRole.MULTIPLIER,
                BandRole.TOLERANCE
            ),
            rolesFor(5)
        )
        assertEquals(
            listOf(
                BandRole.DIGIT,
                BandRole.DIGIT,
                BandRole.DIGIT,
                BandRole.MULTIPLIER,
                BandRole.TOLERANCE,
                BandRole.TCR
            ),
            rolesFor(6)
        )
    }

    // ---- Tích hợp tính toán ----

    @Test
    fun `chọn đủ màu 4 dải qua pick ra 1000 ohm`() {
        val s = BandsState()
        s.addBand()
        s.pick(BandColor.BROWN)
        s.moveActive(1)
        s.pick(BandColor.BLACK)
        s.moveActive(1)
        s.pick(BandColor.RED)
        s.moveActive(1)
        s.pick(BandColor.GOLD)
        val r = s.result
        assertTrue(r is CalcResult.Success)
        assertEquals(1000.0, (r as CalcResult.Success).resistance.ohms, 1e-9)
    }

    @Test
    fun `result vẫn null khi còn dải trống sau khi thêm bớt dải`() {
        val s = BandsState()
        s.addBand()
        s.pick(BandColor.BROWN)
        s.moveActive(1)
        s.pick(BandColor.BLACK)
        s.moveActive(1)
        s.pick(BandColor.RED)
        assertNull(s.result)
    }

    // ---- Bridge API cũ (BandsScreen v1 còn gọi) ----

    @Test
    fun `pick overload cũ theo index gán trực tiếp không đổi activeBand`() {
        val s = BandsState()
        s.pick(2, BandColor.RED)
        assertEquals(BandColor.RED, s.selected[2])
        assertEquals(0, s.activeBand)
    }
}
