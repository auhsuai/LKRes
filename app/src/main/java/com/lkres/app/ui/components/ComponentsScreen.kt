package com.lkres.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lkres.app.core.Component
import com.lkres.app.core.ComponentCatalog
import com.lkres.app.core.ComponentCategory

// 3 cấp điều hướng nội bộ trong tab "Linh kiện": root -> danh mục -> chi tiết.
// Enum là Serializable nên rememberSaveable lưu được qua Bundle.
private enum class ComponentsLevel { ROOT, CATEGORY, DETAIL }

// Icon mũi tên trái vẽ tay (không thêm dependency icon), dùng chung cho 2 màn hình.
internal fun backArrowIcon(): ImageVector = ImageVector.Builder(
    name = "BackArrowIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 2f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
) {
    moveTo(15f, 4.5f)
    lineTo(7.5f, 12f)
    lineTo(15f, 19.5f)
}.build()

// Host tab "Linh kiện": giữ 3 cấp + các query bằng rememberSaveable để đổi tab
// qua lại (NavHost saveState/restoreState) không mất vị trí.
@Composable
fun ComponentsScreen() {
    var level by rememberSaveable { mutableStateOf(ComponentsLevel.ROOT) }
    var categoryName by rememberSaveable { mutableStateOf("") }
    var componentName by rememberSaveable { mutableStateOf("") }
    var rootQuery by rememberSaveable { mutableStateOf("") }
    var categoryQuery by rememberSaveable { mutableStateOf("") }

    val category = ComponentCategory.entries.firstOrNull { it.name == categoryName }
    val component = ComponentCatalog.all.firstOrNull { it.name == componentName }

    fun goBack() {
        when (level) {
            ComponentsLevel.DETAIL -> {
                if (categoryName.isEmpty()) {
                    level = ComponentsLevel.ROOT
                } else {
                    componentName = ""
                    level = ComponentsLevel.CATEGORY
                }
            }
            ComponentsLevel.CATEGORY -> {
                categoryName = ""
                level = ComponentsLevel.ROOT
            }
            ComponentsLevel.ROOT -> Unit
        }
    }

    // Ở ROOT không chặn: back mặc định thoát app như bình thường.
    BackHandler(enabled = level != ComponentsLevel.ROOT) { goBack() }

    when {
        level == ComponentsLevel.DETAIL && component != null ->
            ComponentDetailScreen(component = component, onBack = { goBack() })

        level == ComponentsLevel.CATEGORY && category != null ->
            CategoryContent(
                category = category,
                query = categoryQuery,
                onQueryChange = { categoryQuery = it },
                onBack = { goBack() },
                onOpenComponent = { c ->
                    componentName = c.name
                    level = ComponentsLevel.DETAIL
                },
            )

        else -> RootContent(
            query = rootQuery,
            onQueryChange = { rootQuery = it },
            onOpenCategory = { cat ->
                categoryName = cat.name
                categoryQuery = ""
                level = ComponentsLevel.CATEGORY
            },
            onOpenComponent = { c ->
                componentName = c.name
                categoryName = ""
                level = ComponentsLevel.DETAIL
            },
        )
    }
}

@Composable
private fun RootContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenCategory: (ComponentCategory) -> Unit,
    onOpenComponent: (Component) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Tìm linh kiện") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (query.isBlank()) {
            ComponentCategory.entries.forEach { cat ->
                Text(
                    "${cat.label} (${ComponentCatalog.byCategory(cat).size})",
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCategory(cat) }
                        .padding(vertical = 10.dp),
                )
            }
        } else {
            val results = ComponentCatalog.search(query)
            if (results.isEmpty()) {
                Text("Không tìm thấy linh kiện")
            } else {
                results.forEach { c ->
                    ComponentRow(component = c, onClick = { onOpenComponent(c) })
                }
            }
        }
    }
}

// Trang danh mục (VD BJT): search CHỈ trong danh mục đang mở.
@Composable
private fun CategoryContent(
    category: ComponentCategory,
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenComponent: (Component) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(backArrowIcon(), contentDescription = "Quay lại")
            }
            Text(category.label, style = MaterialTheme.typography.headlineSmall)
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Tìm trong ${category.label}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        val list = if (query.isBlank()) {
            ComponentCatalog.byCategory(category)
        } else {
            ComponentCatalog.search(query, category)
        }
        if (list.isEmpty()) {
            Text("Không tìm thấy linh kiện")
        } else {
            list.forEach { c ->
                ComponentRow(component = c, onClick = { onOpenComponent(c) })
            }
        }
    }
}

@Composable
private fun ComponentRow(component: Component, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Text(
            component.name,
            style = MaterialTheme.typography.titleMedium,
            textDecoration = TextDecoration.Underline,
        )
        Text(
            "${component.kind} · ${component.packageName}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
