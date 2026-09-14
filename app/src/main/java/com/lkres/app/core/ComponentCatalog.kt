package com.lkres.app.core

enum class ComponentCategory(val label: String) { BJT("BJT"), FET("FET") }

data class ComponentPin(val symbol: String, val function: String)

data class Component(
    val name: String,
    val fullName: String,
    val category: ComponentCategory,
    val kind: String,
    val packageName: String,
    val pins: List<ComponentPin>,
)

object ComponentCatalog {

    private val bjtPins = listOf(
        ComponentPin("E", "cực phát (Emitter)"),
        ComponentPin("C", "cực thu (Collector)"),
        ComponentPin("B", "cực nền (Base)"),
    )

    val all: List<Component> = listOf(
        Component(
            name = "C1815",
            fullName = "2SC1815",
            category = ComponentCategory.BJT,
            kind = "BJT NPN",
            packageName = "TO-92",
            pins = bjtPins,
        ),
        Component(
            name = "A1015",
            fullName = "2SA1015",
            category = ComponentCategory.BJT,
            kind = "BJT PNP",
            packageName = "TO-92",
            pins = bjtPins,
        ),
        Component(
            name = "K30A",
            fullName = "2SK30A",
            category = ComponentCategory.FET,
            kind = "JFET kênh N",
            packageName = "TO-92",
            pins = listOf(
                ComponentPin("S", "cực nguồn (Source)"),
                ComponentPin("G", "cực cổng (Gate)"),
                ComponentPin("D", "cực máng (Drain)"),
            ),
        ),
    )

    fun byCategory(category: ComponentCategory): List<Component> =
        all.filter { it.category == category }

    fun search(query: String, category: ComponentCategory? = null): List<Component> {
        val q = query.trim().uppercase()
        if (q.isEmpty()) return emptyList()
        return all.filter { c ->
            (category == null || c.category == category) &&
                (c.name.contains(q) ||
                    c.fullName.contains(q) ||
                    c.kind.contains(q) ||
                    c.category.label.contains(q))
        }
    }
}
