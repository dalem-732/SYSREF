package com.sysref.commands.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sysref.commands.CommandEntry
import com.sysref.commands.FavoritesStorage
import com.sysref.commands.SearchFilter
import com.sysref.commands.loadCommands
import com.sysref.commands.toggleFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val commands: List<CommandEntry>) : LoadState
    data class Failed(val message: String) : LoadState
}

@Composable
fun SysRefRoot(
    favoritesStorage: FavoritesStorage,
    initialMode: String = Screen.BROWSE.id,
    initialPlatform: String? = null,
    initialCategory: String? = null,
    initialQuery: String = "",
    initialFilter: String = SearchFilter.ALL.id,
    initialFavoritesOnly: Boolean = false,
    shortcuts: Flow<AppShortcut> = emptyFlow(),
) {
    val state by produceState<LoadState>(LoadState.Loading) {
        value = try {
            LoadState.Ready(loadCommands())
        } catch (error: Exception) {
            LoadState.Failed(error.message ?: error::class.simpleName.orEmpty())
        }
    }
    val target = remember(initialMode, initialPlatform, initialCategory) {
        launchTarget(initialMode, initialPlatform, initialCategory)
    }
    SysRefTheme {
        SysRefApp(
            commands = (state as? LoadState.Ready)?.commands,
            loadError = (state as? LoadState.Failed)?.message,
            favoritesStorage = favoritesStorage,
            initialScreen = target.screen,
            initialPlatform = target.platformId,
            initialCategory = target.categoryId,
            initialQuery = initialQuery,
            initialFilter = SearchFilter.fromId(initialFilter),
            initialFavoritesOnly = initialFavoritesOnly,
            shortcuts = shortcuts,
        )
    }
}

@Composable
fun SysRefApp(
    commands: List<CommandEntry>?,
    loadError: String? = null,
    favoritesStorage: FavoritesStorage,
    initialScreen: Screen = Screen.BROWSE,
    initialPlatform: String? = null,
    initialCategory: String? = null,
    initialQuery: String = "",
    initialFilter: SearchFilter = SearchFilter.ALL,
    initialFavoritesOnly: Boolean = false,
    shortcuts: Flow<AppShortcut> = emptyFlow(),
) {
    var screen by rememberSaveable { mutableStateOf(initialScreen) }
    var platformId by rememberSaveable { mutableStateOf(initialPlatform) }
    var categoryId by rememberSaveable { mutableStateOf(initialCategory) }
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var filter by rememberSaveable { mutableStateOf(initialFilter) }
    var favoritesOnly by rememberSaveable { mutableStateOf(initialFavoritesOnly) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var favorites by remember { mutableStateOf(favoritesStorage.load()) }
    var focusSearchTick by remember { mutableIntStateOf(0) }

    fun goBack() {
        when {
            screen == Screen.SEARCH -> screen = Screen.BROWSE
            categoryId != null -> categoryId = null
            platformId != null -> platformId = null
        }
    }

    SystemBackHandler(enabled = screen == Screen.SEARCH || platformId != null) { goBack() }

    LaunchedEffect(shortcuts) {
        shortcuts.collect { shortcut ->
            when (shortcut) {
                AppShortcut.FocusSearch -> {
                    screen = Screen.SEARCH
                    focusSearchTick++
                }
                AppShortcut.Escape ->
                    if (screen == Screen.SEARCH && query.isNotEmpty()) query = "" else goBack()
            }
        }
    }

    fun notify(message: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    fun copy(entry: CommandEntry) {
        clipboard.setText(AnnotatedString(entry.command))
        notify(if (entry.danger) "скопировано, осторожно: ${entry.command}" else "скопировано: ${entry.command}")
    }

    fun toggleStar(entry: CommandEntry) {
        val added = entry.id !in favorites
        favorites = toggleFavorite(favorites, entry.id)
        favoritesStorage.save(favorites)
        notify(if (added) "★ в избранном: ${entry.command}" else "убрано из избранного: ${entry.command}")
    }

    val favoritesCount = remember(commands, favorites) {
        commands?.count { it.id in favorites } ?: 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TermBg)
            .systemBarsPadding()
            .imePadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Header(count = commands?.size)
            Rule()
            ModeSwitch(screen = screen, onScreen = { screen = it })
            Rule()
            Box(Modifier.weight(1f).fillMaxWidth()) {
                val currentPlatform = platformId
                val currentCategory = categoryId
                when {
                    loadError != null -> CenterNote("ошибка чтения базы\n$loadError")
                    commands == null -> CenterNote("загрузка базы...")
                    screen == Screen.SEARCH -> SearchPane(
                        commands = commands,
                        query = query,
                        onQuery = { query = it },
                        filter = filter,
                        onFilter = { filter = it },
                        favoritesOnly = favoritesOnly,
                        onFavoritesOnly = { favoritesOnly = it },
                        favorites = favorites,
                        onToggleFavorite = ::toggleStar,
                        onCopy = ::copy,
                        focusSearchTick = focusSearchTick,
                    )
                    currentPlatform == null -> PlatformPane(
                        commands = commands,
                        favoritesCount = favoritesCount,
                        onOpen = { platformId = it },
                        onOpenFavorites = {
                            favoritesOnly = true
                            screen = Screen.SEARCH
                        },
                    )
                    currentCategory == null -> CategoryPane(
                        commands = commands,
                        platformId = currentPlatform,
                        onBack = { platformId = null },
                        onOpen = { categoryId = it },
                    )
                    else -> CommandPane(
                        commands = commands,
                        platformId = currentPlatform,
                        categoryId = currentCategory,
                        onBack = { categoryId = null },
                        favorites = favorites,
                        onToggleFavorite = ::toggleStar,
                        onCopy = ::copy,
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
        ) { data ->
            Text(
                text = data.visuals.message,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TermPanel)
                    .border(1.dp, Amber)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                color = Amber,
                fontFamily = Mono,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
