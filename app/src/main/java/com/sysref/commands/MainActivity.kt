package com.sysref.commands

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.core.view.WindowCompat
import com.sysref.commands.ui.SysRefApp
import com.sysref.commands.ui.SysRefTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        val screen = intent.getStringExtra("screen") ?: "browse"
        val platform = intent.getStringExtra("platform")
        val category = intent.getStringExtra("category")
        val query = intent.getStringExtra("query").orEmpty()
        val filter = intent.getStringExtra("filter") ?: "all"
        setContent {
            val state by produceState<LoadState>(LoadState.Loading) {
                value = try {
                    val commands = withContext(Dispatchers.IO) { CommandRepository.load(this@MainActivity) }
                    LoadState.Ready(commands)
                } catch (error: Exception) {
                    LoadState.Failed(error.message ?: error.javaClass.simpleName)
                }
            }
            SysRefTheme {
                val ready = state as? LoadState.Ready
                val failed = state as? LoadState.Failed
                SysRefApp(
                    commands = ready?.commands,
                    loadError = failed?.message,
                    initialMode = screen,
                    initialPlatform = platform,
                    initialCategory = category,
                    initialQuery = query,
                    initialFilter = filter,
                )
            }
        }
    }
}

private sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val commands: List<CommandEntry>) : LoadState
    data class Failed(val message: String) : LoadState
}
