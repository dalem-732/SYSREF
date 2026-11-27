package com.sysref.commands

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.sysref.commands.ui.SysRefRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        val favoritesStorage = SharedPrefsFavoritesStorage(this)
        val screen = intent.safeString("screen")
        val platform = intent.safeString("platform")
        val category = intent.safeString("category")
        val query = intent.safeString("query").orEmpty().take(MAX_QUERY_LENGTH)
        val filter = intent.safeString("filter")
        val favoritesOnly = runCatching { intent.getBooleanExtra("favorites", false) }.getOrDefault(false)
        setContent {
            SysRefRoot(
                favoritesStorage = favoritesStorage,
                initialMode = screen.orEmpty(),
                initialPlatform = platform,
                initialCategory = category,
                initialQuery = query,
                initialFilter = filter.orEmpty(),
                initialFavoritesOnly = favoritesOnly,
            )
        }
    }

    private fun Intent.safeString(key: String): String? = runCatching { getStringExtra(key) }.getOrNull()

    private companion object {
        const val MAX_QUERY_LENGTH = 200
    }
}
