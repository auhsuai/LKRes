package com.lkres.app.ui.smd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lkres.app.core.ResistorFormat
import com.lkres.app.core.SmdErrorKind
import com.lkres.app.core.SmdParser
import com.lkres.app.core.SmdResult

@Composable
fun SmdScreen() {
    var code by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Mã SMD (VD: 472, 4R7, 01C)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        when (val r = SmdParser.parse(code)) {
            is SmdResult.Success -> Text(
                ResistorFormat.format(r.resistance.ohms),
                style = MaterialTheme.typography.headlineMedium
            )
            is SmdResult.Error ->
                if (code.isNotBlank()) {
                    Text(r.kind.message, color = MaterialTheme.colorScheme.error)
                }
        }
        Text(
            "3 số: 472 = 4.7 kΩ · 4 số: 4702 = 47 kΩ · R: 4R7 = 4.7 Ω · EIA-96: 01C = 10 kΩ",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
