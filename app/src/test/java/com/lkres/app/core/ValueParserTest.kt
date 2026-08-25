package com.lkres.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValueParserTest {

    private fun ohms(raw: String): Double {
        val r = ValueParser.parse(raw)
        assertTrue("'$raw' phải Success, thực tế $r", r is ValueParseResult.Success)
        return (r as ValueParseResult.Success).ohms
    }

    private fun err(raw: String): ValueErrorKind {
        val r = ValueParser.parse(raw)
        assertTrue("'$raw' phải Error, thực tế $r", r is ValueParseResult.Error)
        return (r as ValueParseResult.Error).kind
    }

    @Test fun `số trần 4700`() = assertEquals(4700.0, ohms("4700"), 1e-9)
    @Test fun `có đơn vị 1k`() = assertEquals(1000.0, ohms("1k"), 1e-9)
    @Test fun `hoa thường 1K và 1k bằng nhau`() = assertEquals(ohms("1k"), ohms("1K"), 1e-9)
    @Test fun `phẩy thập phân 4_7k`() = assertEquals(4700.0, ohms("4,7k"), 1e-9)
    @Test fun `chấm thập phân 4_7k`() = assertEquals(4700.0, ohms("4.7k"), 1e-9)
    @Test fun `kiểu giữa 4k7`() = assertEquals(4700.0, ohms("4k7"), 1e-9)
    @Test fun `kiểu giữa 1k5`() = assertEquals(1500.0, ohms("1k5"), 1e-9)
    @Test fun `mega 2M2`() = assertEquals(2_200_000.0, ohms("2M2"), 1e-9)
    @Test fun `giga 1g`() = assertEquals(1e9, ohms("1g"), 1e-9)
    @Test fun `có ký tự omega`() = assertEquals(4700.0, ohms("4,7kΩ"), 1e-9)
    @Test fun `có chữ ohm`() = assertEquals(1000.0, ohms("1k ohm"), 1e-9)
    @Test fun `có khoảng trắng đầu cuối`() = assertEquals(4700.0, ohms("  4,7k "), 1e-9)
    @Test fun `dưới 1 ôm 0,47`() = assertEquals(0.47, ohms("0,47"), 1e-9)
    @Test fun `rỗng là EMPTY`() = assertEquals(ValueErrorKind.EMPTY, err("   "))
    @Test fun `ký tự lạ là INVALID_FORMAT`() = assertEquals(ValueErrorKind.INVALID_FORMAT, err("4a7"))
    @Test fun `hai dấu phẩy là INVALID_FORMAT`() = assertEquals(ValueErrorKind.INVALID_FORMAT, err("4,7,0"))
    @Test fun `số 0 là NON_POSITIVE`() = assertEquals(ValueErrorKind.NON_POSITIVE, err("0"))
    @Test fun `0k là NON_POSITIVE`() = assertEquals(ValueErrorKind.NON_POSITIVE, err("0k"))
    @Test fun `4R7 là SMD_STYLE`() = assertEquals(ValueErrorKind.SMD_STYLE, err("4R7"))
    @Test fun `r thường vẫn nhận ra SMD`() = assertEquals(ValueErrorKind.SMD_STYLE, err("4r7"))
}
