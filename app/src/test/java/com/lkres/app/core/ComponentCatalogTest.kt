package com.lkres.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentCatalogTest {

    private fun namesOf(query: String, category: ComponentCategory? = null): List<String> =
        ComponentCatalog.search(query, category).map { it.name }

    private fun component(name: String): Component =
        ComponentCatalog.all.first { it.name == name }

    @Test
    fun `query rỗng trả danh sách rỗng`() {
        assertEquals(emptyList<String>(), namesOf(""))
    }

    @Test
    fun `query toàn khoảng trắng trả danh sách rỗng`() {
        assertEquals(emptyList<String>(), namesOf("   "))
    }

    @Test
    fun `query không khớp trả danh sách rỗng`() {
        assertEquals(emptyList<String>(), namesOf("xyz"))
    }

    @Test
    fun `khớp tên không phân biệt hoa thường`() {
        assertEquals(listOf("C1815"), namesOf("c1815"))
        assertEquals(listOf("C1815"), namesOf("C1815"))
    }

    @Test
    fun `khớp tên đầy đủ 2SC1815`() {
        assertEquals(listOf("C1815"), namesOf("2SC1815"))
    }

    @Test
    fun `khớp một phần tên 1815`() {
        assertEquals(listOf("C1815"), namesOf("1815"))
    }

    @Test
    fun `khớp K30A và tên đầy đủ 2SK30A`() {
        assertEquals(listOf("K30A"), namesOf("k30a"))
        assertEquals(listOf("K30A"), namesOf("2SK30A"))
    }

    @Test
    fun `khớp một phần tên A1015`() {
        assertEquals(listOf("A1015"), namesOf("1015"))
    }

    @Test
    fun `khớp kind JFET`() {
        assertEquals(listOf("K30A"), namesOf("jfet"))
    }

    @Test
    fun `khớp kind NPN`() {
        assertEquals(listOf("C1815"), namesOf("npn"))
    }

    @Test
    fun `khớp nhãn danh mục BJT đúng thứ tự`() {
        assertEquals(listOf("C1815", "A1015"), namesOf("bjt"))
    }

    @Test
    fun `scope BJT loại trừ K30A`() {
        assertEquals(emptyList<String>(), namesOf("k30a", ComponentCategory.BJT))
    }

    @Test
    fun `scope FET loại trừ C1815`() {
        assertEquals(emptyList<String>(), namesOf("1815", ComponentCategory.FET))
    }

    @Test
    fun `scope BJT tìm được A1015`() {
        assertEquals(listOf("A1015"), namesOf("1015", ComponentCategory.BJT))
    }

    @Test
    fun `byCategory trả đúng linh kiện từng danh mục`() {
        assertEquals(
            listOf("C1815", "A1015"),
            ComponentCatalog.byCategory(ComponentCategory.BJT).map { it.name }
        )
        assertEquals(listOf("K30A"), ComponentCatalog.byCategory(ComponentCategory.FET).map { it.name })
    }

    @Test
    fun `thứ tự all là C1815 A1015 K30A`() {
        assertEquals(listOf("C1815", "A1015", "K30A"), ComponentCatalog.all.map { it.name })
    }

    @Test
    fun `pin C1815 và A1015 là E C B`() {
        assertEquals(listOf("E", "C", "B"), component("C1815").pins.map { it.symbol })
        assertEquals(listOf("E", "C", "B"), component("A1015").pins.map { it.symbol })
    }

    @Test
    fun `pin K30A là S G D`() {
        assertEquals(listOf("S", "G", "D"), component("K30A").pins.map { it.symbol })
    }

    @Test
    fun `mỗi component đúng 3 pin và function không rỗng`() {
        ComponentCatalog.all.forEach { c ->
            assertEquals("${c.name} phải có đúng 3 pin", 3, c.pins.size)
            c.pins.forEach { p ->
                assertTrue("${c.name} pin ${p.symbol} thiếu function", p.function.isNotBlank())
            }
        }
    }
}
