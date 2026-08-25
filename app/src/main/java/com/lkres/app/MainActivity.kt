package com.lkres.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lkres.app.core.BandColor
import com.lkres.app.ui.resistor.ResistorCanvas
import com.lkres.app.ui.theme.LKResTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LKResTheme {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ResistorCanvas(
                        bandColors = listOf(
                            BandColor.YELLOW,
                            BandColor.VIOLET,
                            BandColor.RED,
                            BandColor.GOLD
                        ),
                        modifier = Modifier.fillMaxSize().aspectRatio(2.4f)
                    )
                }
            }
        }
    }
}
