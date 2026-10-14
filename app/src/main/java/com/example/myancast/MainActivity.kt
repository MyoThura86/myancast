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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    // ★ Phase 6 (#1) — SharedPreferences ထဲ သိမ်းထားတဲ့ setting — app ပိတ်ပြီး ပြန်ဖွင့်လည်း မပျောက်
    val app = LocalContext.current.applicationContext as MyanCastApp
    val settings by app.settingsRepository.settings.collectAsStateWithLifecycle()
    val config = AppConfig(zawgyi = false, darkMode = settings.darkMode)

    MyanCastTheme(config = config) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MyanCastNavHost(navController = rememberNavController())
        }
    }
}
