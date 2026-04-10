package com.ae.devlens.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ae.devlens.AEDevLens
import com.ae.devlens.AEDevLensProvider
import com.ae.devlens.plugins.logs.LogsPlugin
import com.ae.devlens.plugins.logs.model.LogLevel

@Composable
fun App() {
    MaterialTheme {
        AEDevLensProvider(
            enabled = true,
            inspector = AEDevLens.default.apply {
                if (getPlugin<LogsPlugin>() == null) {
                    install(LogsPlugin())
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("AEDevLens Sample App")
                Text("The Developer Tool SDK is active!")
                
                Button(onClick = {
                    AEDevLens.default.log(LogLevel.INFO, "SampleApp", "Hello from the sample app!")
                }) {
                    Text("Trigger Info Log")
                }
                
                Button(onClick = {
                    AEDevLens.default.log(LogLevel.ERROR, "SampleApp", "Uh oh! Something went wrong!")
                }) {
                    Text("Trigger Error Log")
                }
            }
        }
    }
}
