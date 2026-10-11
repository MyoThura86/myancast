// MainActivity.kt
package com.example.myancast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myancast.ui.navigation.MyanCastNavHost
import com.example.myancast.ui.theme.AppConfig
import com.example.myancast.ui.theme.MyanCastTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyanCastRoot()
        }
    }
}

@Composable
private fun MyanCastRoot() {
    var config by rememberSaveable(stateSaver = AppConfig.Saver) {
        mutableStateOf(AppConfig(zawgyi = false, darkMode = true))
    }

    MyanCastTheme(config = config) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MyanCastNavHost(
                navController = rememberNavController(),
                config = config,
                onConfigChange = { config = it }
            )
        }
    }
}