package com.lkres.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValueToColorsTest {

    @Test
    fun `4700 ohm ra 4 dải vàng-tím-đỏ`() {
        val r = ValueToColors.encode(4700.0)
        assertTrue(r is EncodingResult.Encodable)
        r as EncodingResult.Encodable
        val v4 = r.variants.first { it.bandCount == 4 }
        assertEquals(listOf(BandColor.YELLOW, BandColor.VIOLET, BandColor.RED), v4.colors)
    }

    @Test
    fun `4700 ohm có thêm biến thể 5 dải vàng-tím-đen-nâu`() {
        val r = ValueToColors.encode(4700.0) as EncodingResult.Encodable
        val v5 = r.variants.first { it.bandCount == 5 }
        assertEquals(listOf(BandColor.YELLOW, BandColor.VIOLET, BandColor.BLACK, BandColor.BROWN), v5.colors)
    }

    @Test
    fun `4_7 ohm dùng multiplier vàng kim`() {
        val r = ValueToColors.encode(4.7) as EncodingResult.Encodable
        val v4 = r.variants.first { it.bandCount == 4 }
        assertEquals(listOf(BandColor.YELLOW, BandColor.VIOLET, BandColor.GOLD), v4.colors)
    }

    @Test
    fun `0_47 ohm dùng multiplier bạc`() {
        val r = ValueToColors.encode(0.47) as EncodingResult.Encodable
        val v4 = r.variants.first { it.bandCount == 4 }
        assertEquals(listOf(BandColor.YELLOW, BandColor.VIOLET, BandColor.SILVER), v4.colors)
    }

    @Test
    fun `47500 chỉ encode được 5 dải 3 digit`() {
        val r = ValueToColors.encode(47500.0) as EncodingResult.Encodable
        assertEquals(listOf(5, 6), r.variants.map { it.bandCount })
        val v5 = r.variants.first()
        assertEquals(listOf(BandColor.YELLOW, BandColor.VIOLET, BandColor.GREEN, BandColor.RED), v5.colors)
    }

    @Test
    fun `1234 không encode được gợi ý 1_2k gần nhất E24`() {
        val r = ValueToColors.encode(1234.0)
        assertTrue(r is EncodingResult.NotEncodable)
        assertEquals(1200.0, (r as EncodingResult.NotEncodable).nearestE24, 1e-9)
    }

    @Test
    fun `47k ra 4 dải vàng-tím-cam`() {
        val r = ValueToColors.encode(47000.0) as EncodingResult.Encodable
        val v4 = r.variants.first { it.bandCount == 4 }
        assertEquals(listOf(BandColor.YELLOW, BandColor.VIOLET, BandColor.ORANGE), v4.colors)
    }
}
