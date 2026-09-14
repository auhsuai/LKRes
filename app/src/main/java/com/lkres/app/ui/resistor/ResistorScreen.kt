package com.lkres.app.ui.resistor

import androidx.compose.runtime.Composable
import com.lkres.app.data.LkResStore
import com.lkres.app.ui.bands.BandsScreen
import com.lkres.app.ui.smd.SmdScreen

// Host tab "Điện trở": chuyển giữa Trở cắm (dải màu) và Trở dán (SMD).
// Chế độ nằm trong LkResStore (persist DataStore) — không dùng remember
// để đổi tab rồi quay lại vẫn giữ đúng chế độ.
@Composable
fun ResistorScreen() {
    when (LkResStore.resistorView) {
        ResistorView.BANDS -> BandsScreen(onSwapView = { LkResStore.setResistorView(ResistorView.SMD) })
        ResistorView.SMD -> SmdScreen(onSwapView = { LkResStore.setResistorView(ResistorView.BANDS) })
    }
}
