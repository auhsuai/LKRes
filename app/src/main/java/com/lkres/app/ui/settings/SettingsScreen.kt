package com.lkres.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lkres.app.data.LkResStore
import com.lkres.app.ui.bands.BandsMode

private const val APP_VERSION_NOTE = "LKRes · Phiên bản 1.1"

@Composable
fun SettingsScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineSmall)

        Text("Chế độ chọn màu", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = LkResStore.bands.mode == BandsMode.AUTO,
                onClick = {
                    LkResStore.bands.setMode(BandsMode.AUTO)
                    LkResStore.persistBands()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Tự động") }
            SegmentedButton(
                selected = LkResStore.bands.mode == BandsMode.MANUAL,
                onClick = {
                    LkResStore.bands.setMode(BandsMode.MANUAL)
                    LkResStore.persistBands()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Thủ công") }
        }

        HorizontalDivider()

        ToggleRow(
            title = "Hiện nhiều tổ hợp màu khi tìm kiếm",
            checked = LkResStore.parallelResults,
            onChange = LkResStore::setParallelResults
        )

        HorizontalDivider()

        ToggleRow(
            title = "Màn hình luôn bật khi mở ứng dụng",
            checked = LkResStore.keepScreenOn,
            onChange = LkResStore::setKeepScreenOn
        )

        HorizontalDivider()

        Text(
            APP_VERSION_NOTE,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
