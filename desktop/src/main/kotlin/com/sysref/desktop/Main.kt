package com.sysref.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sysref.commands.FileFavoritesStorage
import com.sysref.commands.ui.AppShortcut
import com.sysref.commands.ui.SysRefRoot
import kotlinx.coroutines.flow.MutableSharedFlow
import java.awt.Dimension

/**
 * Optional arguments, mostly for screenshots:
 * --screen=browse|search --platform=debian --category=packages --query=nmap --filter=all --favorites
 */
private class LaunchArgs(args: Array<String>) {
    private val values: Map<String, String> = args.filter { it.startsWith("--") && "=" in it }
        .associate { it.removePrefix("--").substringBefore("=") to it.substringAfter("=") }
    val screen: String = values["screen"] ?: "browse"
    val platform: String? = values["platform"]
    val category: String? = values["category"]
    val query: String = values["query"].orEmpty()
    val filter: String = values["filter"] ?: "all"
    val favoritesOnly: Boolean = "--favorites" in args
}

fun main(args: Array<String>) {
    val launch = LaunchArgs(args)
    application {
        val shortcuts = remember { MutableSharedFlow<AppShortcut>(extraBufferCapacity = 8) }
        val storage = remember { FileFavoritesStorage() }
        val icon = remember {
            val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("sysref-icon.png")
            stream?.use { BitmapPainter(loadImageBitmap(it)) }
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = "SYSREF",
            icon = icon,
            state = rememberWindowState(size = DpSize(540.dp, 860.dp)),
            onPreviewKeyEvent = { event ->
                when {
                    event.type != KeyEventType.KeyDown -> false
                    (event.isCtrlPressed || event.isMetaPressed) && event.key == Key.F ->
                        shortcuts.tryEmit(AppShortcut.FocusSearch)
                    event.key == Key.Escape -> shortcuts.tryEmit(AppShortcut.Escape)
                    else -> false
                }
            },
        ) {
            window.minimumSize = Dimension(420, 560)
            SysRefRoot(
                favoritesStorage = storage,
                initialMode = launch.screen,
                initialPlatform = launch.platform,
                initialCategory = launch.category,
                initialQuery = launch.query,
                initialFilter = launch.filter,
                initialFavoritesOnly = launch.favoritesOnly,
                shortcuts = shortcuts,
            )
        }
    }
}
