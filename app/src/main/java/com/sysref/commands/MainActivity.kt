package com.sysref.commands

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
        // Optional launch extras (used for screenshots): screen, platform, category, query, filter, favorites.
        val screen = intent.getStringExtra("screen") ?: "browse"
        val platform = intent.getStringExtra("platform")
        val category = intent.getStringExtra("category")
        val query = intent.getStringExtra("query").orEmpty()
        val filter = intent.getStringExtra("filter") ?: "all"
        val favoritesOnly = intent.getBooleanExtra("favorites", false)
        setContent {
            SysRefRoot(
                favoritesStorage = favoritesStorage,
                initialMode = screen,
                initialPlatform = platform,
                initialCategory = category,
                initialQuery = query,
                initialFilter = filter,
                initialFavoritesOnly = favoritesOnly,
            )
        }
    }
}
