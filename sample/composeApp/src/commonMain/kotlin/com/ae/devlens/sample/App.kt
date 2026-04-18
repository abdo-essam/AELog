package com.ae.devlens.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ae.devlens.AEDevLensProvider
import com.ae.devlens.DevLens
import com.ae.devlens.DevLensSetup
import com.ae.devlens.DevLensUiConfig

@Composable
fun App() {
    // DevLensSetup.init() is idempotent — safe to call on every recomposition
    // It wires AEDevLens.default + DevLens.inspector in one call.
    val inspector = remember { DevLensSetup.init() }

    MaterialTheme {
        AEDevLensProvider(
            inspector = inspector,
            uiConfig = DevLensUiConfig(
                showFloatingButton = true,
                enableLongPress = true,
            ),
            enabled = true,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("AEDevLens Sample App")
                Text("Open DevLens: tap the 🐛 button or long-press anywhere")

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { DevLens.i("SampleApp", "Hello from the sample app!") }) {
                    Text("Trigger Info Log")
                }

                Button(onClick = { DevLens.e("SampleApp", "Uh oh! Something went wrong!") }) {
                    Text("Trigger Error Log")
                }

                Button(onClick = { DevLens.d("SampleApp", "Debug message with details") }) {
                    Text("Trigger Debug Log")
                }

                Button(onClick = { DevLens.w("SampleApp", "Watch out for this!") }) {
                    Text("Trigger Warn Log")
                }
            }
        }
    }
}
