package com.lkres.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lkres.app.ui.bands.BandsScreen
import com.lkres.app.ui.reference.ReferenceScreen
import com.lkres.app.ui.smd.SmdScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private fun stripeIcon(): ImageVector = ImageVector.Builder(
    name = "BandsIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(fill = SolidColor(Color.Black)) {
    moveTo(4f, 5f)
    lineTo(8f, 5f)
    lineTo(8f, 19f)
    lineTo(4f, 19f)
    close()
    moveTo(10f, 5f)
    lineTo(14f, 5f)
    lineTo(14f, 19f)
    lineTo(10f, 19f)
    close()
    moveTo(16f, 5f)
    lineTo(20f, 5f)
    lineTo(20f, 19f)
    lineTo(16f, 19f)
    close()
}.build()

private fun chipIcon(): ImageVector = ImageVector.Builder(
    name = "SmdChipIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(fill = SolidColor(Color.Black)) {
    moveTo(7f, 7f)
    lineTo(17f, 7f)
    lineTo(17f, 17f)
    lineTo(7f, 17f)
    close()
    moveTo(4f, 9f)
    lineTo(7f, 9f)
    lineTo(7f, 11f)
    lineTo(4f, 11f)
    close()
    moveTo(4f, 13f)
    lineTo(7f, 13f)
    lineTo(7f, 15f)
    lineTo(4f, 15f)
    close()
    moveTo(17f, 9f)
    lineTo(20f, 9f)
    lineTo(20f, 11f)
    lineTo(17f, 11f)
    close()
    moveTo(17f, 13f)
    lineTo(20f, 13f)
    lineTo(20f, 15f)
    lineTo(17f, 15f)
    close()
}.build()

private fun bookIcon(): ImageVector = ImageVector.Builder(
    name = "BookIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(fill = SolidColor(Color.Black)) {
    moveTo(4f, 5.5f)
    curveTo(6f, 4.5f, 8.5f, 4.5f, 10.5f, 5.5f)
    lineTo(10.5f, 19f)
    curveTo(8.5f, 18f, 6f, 18f, 4f, 19f)
    close()
    moveTo(20f, 5.5f)
    curveTo(18f, 4.5f, 15.5f, 4.5f, 13.5f, 5.5f)
    lineTo(13.5f, 19f)
    curveTo(15.5f, 18f, 18f, 18f, 20f, 19f)
    close()
}.build()

private val TABS = listOf(
    Tab("bands", "Dải màu", stripeIcon()),
    Tab("smd", "SMD", chipIcon()),
    Tab("reference", "Tham khảo", bookIcon()),
)

@Composable
fun LKResApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(bottomBar = {
        NavigationBar {
            TABS.forEach { tab ->
                NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                )
            }
        }
    }) { padding ->
        NavHost(
            navController = navController,
            startDestination = "bands",
            modifier = Modifier.padding(padding),
        ) {
            composable("bands") { BandsScreen() }
            composable("smd") { SmdScreen() }
            composable("reference") { ReferenceScreen() }
        }
    }
}
