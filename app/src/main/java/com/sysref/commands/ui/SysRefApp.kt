package com.sysref.commands.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sysref.commands.CommandEntry
import com.sysref.commands.FavoritesStore
import com.sysref.commands.filterCommands
import com.sysref.commands.Platform
import com.sysref.commands.browsePlatforms
import com.sysref.commands.categoryById
import com.sysref.commands.foldRu
import com.sysref.commands.inBrowse
import com.sysref.commands.platformById
import com.sysref.commands.pluralCommands
import com.sysref.commands.pluralMatches
import com.sysref.commands.toggleFavorite
import kotlinx.coroutines.launch

private data class FilterChip(val id: String, val label: String)

private val filterChips = listOf(
    FilterChip("all", "все"),
    FilterChip("linall", "linux"),
    FilterChip("debian", "deb"),
    FilterChip("rhel", "rhel"),
    FilterChip("suse", "suse"),
    FilterChip("arch", "arch"),
    FilterChip("cmd", "cmd"),
    FilterChip("ps", "ps"),
)

@Composable
fun SysRefApp(
    commands: List<CommandEntry>?,
    loadError: String? = null,
    initialMode: String = "browse",
    initialPlatform: String? = null,
    initialCategory: String? = null,
    initialQuery: String = "",
    initialFilter: String = "all",
    initialFavoritesOnly: Boolean = false,
) {
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var platformId by rememberSaveable { mutableStateOf(initialPlatform) }
    var categoryId by rememberSaveable { mutableStateOf(initialCategory) }
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var filter by rememberSaveable { mutableStateOf(initialFilter) }
    var favoritesOnly by rememberSaveable { mutableStateOf(initialFavoritesOnly) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val favoritesStore = remember { FavoritesStore(context) }
    var favorites by remember { mutableStateOf(favoritesStore.load()) }

    BackHandler(enabled = mode == "search" || platformId != null) {
        when {
            mode == "search" -> mode = "browse"
            categoryId != null -> categoryId = null
            platformId != null -> platformId = null
        }
    }

    fun copy(command: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("command", command))
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar("скопировано: $command")
        }
    }

    fun toggleStar(entry: CommandEntry) {
        val added = entry.id !in favorites
        favorites = toggleFavorite(favorites, entry.id)
        favoritesStore.save(favorites)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(if (added) "★ в избранном: ${entry.command}" else "убрано из избранного: ${entry.command}")
        }
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
            ModeSwitch(mode = mode, onMode = { mode = it })
            Rule()
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    loadError != null -> CenterNote("ошибка чтения базы\n$loadError")
                    commands == null -> CenterNote("загрузка базы...")
                    mode == "search" -> SearchPane(
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
                    )
                    platformId == null -> PlatformPane(
                        commands = commands,
                        favoritesCount = commands.count { it.id in favorites },
                        onOpen = { platformId = it },
                        onOpenFavorites = {
                            favoritesOnly = true
                            mode = "search"
                        },
                    )
                    categoryId == null -> CategoryPane(
                        commands = commands,
                        platformId = platformId!!,
                        onBack = { platformId = null },
                        onOpen = { categoryId = it },
                    )
                    else -> CommandPane(
                        commands = commands,
                        platformId = platformId!!,
                        categoryId = categoryId!!,
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

@Composable
private fun Header(count: Int?) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Amber, fontWeight = FontWeight.Bold)) { append("SYSREF") }
                withStyle(SpanStyle(color = PhosphorDim)) { append("  справочник команд  offline") }
            },
            fontFamily = Mono,
            fontSize = 15.sp,
        )
        Text(
            text = if (count == null) "база: ..." else "база: $count команд",
            color = PhosphorMuted,
            fontFamily = Mono,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ModeSwitch(mode: String, onMode: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ModeKey("справочник", mode == "browse") { onMode("browse") }
        ModeKey("поиск", mode == "search") { onMode("search") }
    }
}

@Composable
private fun ModeKey(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Amber else Color.Transparent
    val fg = if (selected) Color.Black else Phosphor
    Text(
        text = "[ $label ]",
        color = fg,
        fontFamily = Mono,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier
            .background(bg)
            .border(1.dp, if (selected) Amber else PhosphorDim)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    )
}

@Composable
private fun Rule() {
    Text(
        text = "-".repeat(80),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        color = Line,
        fontFamily = Mono,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 14.dp),
    )
}

@Composable
private fun CenterNote(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = PhosphorMuted, fontFamily = Mono, fontSize = 14.sp)
    }
}

@Composable
private fun PlatformPane(
    commands: List<CommandEntry>,
    favoritesCount: Int,
    onOpen: (String) -> Unit,
    onOpenFavorites: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { Hint("// выберите платформу") }
        item(key = "favorites") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFavorites)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "> ★ Избранное",
                    color = Amber,
                    fontFamily = Mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Text("$favoritesCount", color = Amber, fontFamily = Mono, fontSize = 13.sp)
            }
            Rule()
        }
        items(browsePlatforms, key = { it.id }) { platform ->
            val count = commands.count { inBrowse(it, platform.id) }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(platform.id) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .heightIn(min = 48.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "> ${platform.title}",
                        color = Phosphor,
                        fontFamily = Mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text("$count", color = Amber, fontFamily = Mono, fontSize = 13.sp)
                }
                Text(
                    platform.blurb,
                    color = PhosphorMuted,
                    fontFamily = Mono,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp),
                )
            }
            Rule()
        }
    }
}

@Composable
private fun CategoryPane(
    commands: List<CommandEntry>,
    platformId: String,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val platform = platformById(platformId)
    val scoped = remember(commands, platformId) { commands.filter { inBrowse(it, platformId) } }
    val categories = remember(scoped) {
        val present = scoped.map { it.category }.toSet()
        com.sysref.commands.Category.entries.filter { it.id in present }
    }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            BackLine("< назад", onBack)
            PathLine(platform.title)
            Hint("// раздел")
        }
        items(categories, key = { it.id }) { category ->
            val count = scoped.count { it.category == category.id }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(category.id) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "> ${category.title}",
                    color = Phosphor,
                    fontFamily = Mono,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Text("$count", color = Amber, fontFamily = Mono, fontSize = 13.sp)
            }
            Rule()
        }
    }
}

@Composable
private fun CommandPane(
    commands: List<CommandEntry>,
    platformId: String,
    categoryId: String,
    onBack: () -> Unit,
    favorites: Set<String>,
    onToggleFavorite: (CommandEntry) -> Unit,
    onCopy: (String) -> Unit,
) {
    val platform = platformById(platformId)
    val category = categoryById(categoryId)
    val rows = remember(commands, platformId, categoryId) {
        commands.filter { inBrowse(it, platformId) && it.category == categoryId }
    }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            BackLine("< назад", onBack)
            PathLine("${platform.title} / ${category.title}")
            Hint("// нажмите на команду — она попадёт в буфер")
        }
        items(rows, key = { "${it.index}-${it.platform}" }) { entry ->
            CommandBlock(
                entry,
                query = "",
                onCopy = onCopy,
                favorite = entry.id in favorites,
                onToggleFavorite = { onToggleFavorite(entry) },
            )
            Rule()
        }
    }
}

@Composable
private fun SearchPane(
    commands: List<CommandEntry>,
    query: String,
    onQuery: (String) -> Unit,
    filter: String,
    onFilter: (String) -> Unit,
    favoritesOnly: Boolean,
    onFavoritesOnly: (Boolean) -> Unit,
    favorites: Set<String>,
    onToggleFavorite: (CommandEntry) -> Unit,
    onCopy: (String) -> Unit,
) {
    val focus = remember { FocusRequester() }
    val results = remember(commands, query, filter, favorites, favoritesOnly) {
        filterCommands(commands, query, filter, favorites, favoritesOnly)
    }
    LaunchedEffect(Unit) {
        if (query.isEmpty() && !favoritesOnly) focus.requestFocus()
    }
    Column(Modifier.fillMaxSize()) {
        PromptField(query = query, onQuery = onQuery, focusRequester = focus)
        FilterRow(
            selected = filter,
            onSelect = onFilter,
            favoritesOnly = favoritesOnly,
            onFavoritesOnly = onFavoritesOnly,
        )
        Rule()
        val blank = query.trim().isEmpty()
        when {
            favoritesOnly && favorites.isEmpty() ->
                CenterNote("избранное пока пусто\n\nнажмите ☆ справа от команды,\nчтобы она появилась здесь")
            !favoritesOnly && blank ->
                CenterNote("введите фрагмент команды\nили слово на русском\n\nпримеры:  journalctl\n          файрвол\n          robocopy")
            results.isEmpty() && favoritesOnly && blank ->
                CenterNote("в избранном нет команд\nдля этой платформы\n\nвыберите чип «все»")
            results.isEmpty() && favoritesOnly ->
                CenterNote("в избранном нет совпадений\n\nснимите фильтр ★ избранное,\nчтобы искать по всей базе")
            results.isEmpty() -> CenterNote("нет совпадений")
            else -> LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Hint(
                        if (favoritesOnly && blank) "// ★ избранное: ${pluralCommands(results.size)}"
                        else "// ${pluralMatches(results.size)}",
                    )
                }
                items(results, key = { it.id }) { entry ->
                    CommandBlock(
                        entry,
                        query = query.trim(),
                        onCopy = onCopy,
                        showMeta = true,
                        favorite = entry.id in favorites,
                        onToggleFavorite = { onToggleFavorite(entry) },
                    )
                    Rule()
                }
            }
        }
    }
}

@Composable
private fun PromptField(
    query: String,
    onQuery: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    val blink = rememberInfiniteTransition(label = "cursor")
    val alpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "cursor-alpha",
    )
    BasicTextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        textStyle = TextStyle(
            color = Phosphor,
            fontFamily = Mono,
            fontSize = 16.sp,
        ),
        cursorBrush = SolidColor(Color.Transparent),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            autoCorrectEnabled = false,
        ),
        decorationBox = { inner ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("> ", color = Amber, fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("команда или слово", color = PhosphorDim, fontFamily = Mono, fontSize = 16.sp)
                    }
                    inner()
                }
                Text("█", color = Amber.copy(alpha = alpha), fontFamily = Mono, fontSize = 16.sp)
            }
        },
    )
}

@Composable
private fun FilterRow(
    selected: String,
    onSelect: (String) -> Unit,
    favoritesOnly: Boolean,
    onFavoritesOnly: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Works on top of the platform chip and the query, so it is a toggle, not one more radio chip.
        Text(
            text = "★ избранное",
            color = if (favoritesOnly) Color.Black else Amber,
            fontFamily = Mono,
            fontSize = 12.sp,
            modifier = Modifier
                .background(if (favoritesOnly) Amber else Color.Transparent)
                .border(1.dp, Amber)
                .toggleable(value = favoritesOnly, role = Role.Checkbox, onValueChange = onFavoritesOnly)
                .semantics { contentDescription = "Только избранное" }
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
        filterChips.forEach { chip ->
            val on = chip.id == selected
            Text(
                text = chip.label,
                color = if (on) Color.Black else PhosphorMuted,
                fontFamily = Mono,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(if (on) Amber else Color.Transparent)
                    .border(1.dp, if (on) Amber else PhosphorDim)
                    .clickable { onSelect(chip.id) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun CommandBlock(
    entry: CommandEntry,
    query: String,
    onCopy: (String) -> Unit,
    showMeta: Boolean = true,
    favorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth()) {
        CommandText(entry, query, onCopy, showMeta, Modifier.weight(1f))
        if (onToggleFavorite != null) {
            StarToggle(favorite = favorite, onToggle = onToggleFavorite)
        }
    }
}

@Composable
private fun StarToggle(favorite: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .toggleable(value = favorite, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics {
                contentDescription = if (favorite) "Убрать из избранного" else "Добавить в избранное"
                stateDescription = if (favorite) "в избранном" else "не в избранном"
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (favorite) "★" else "☆",
            color = if (favorite) Amber else PhosphorDim,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun CommandText(
    entry: CommandEntry,
    query: String,
    onCopy: (String) -> Unit,
    showMeta: Boolean,
    modifier: Modifier,
) {
    val platform = platformById(entry.platform)
    val category = categoryById(entry.category)
    Column(
        modifier
            .clickable { onCopy(entry.command) }
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Text(
            text = highlight(entry.command, query, Phosphor, Amber),
            fontFamily = Mono,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = highlight(entry.description, query, PhosphorSoft, Amber),
            fontFamily = Mono,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = PhosphorSoft,
        )
        if (showMeta) {
            Text(
                text = highlight(
                    "${platform.title} · ${category.title}",
                    query,
                    PhosphorMuted,
                    Amber,
                ),
                fontFamily = Mono,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun BackLine(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Amber,
        fontFamily = Mono,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}

@Composable
private fun PathLine(path: String) {
    Text(
        text = path,
        color = PhosphorDim,
        fontFamily = Mono,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        color = PhosphorDim,
        fontFamily = Mono,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

private fun highlight(text: String, query: String, base: Color, hit: Color) = buildAnnotatedString {
    val needle = foldRu(query.trim())
    if (needle.isEmpty()) {
        withStyle(SpanStyle(color = base)) { append(text) }
        return@buildAnnotatedString
    }
    val hay = foldRu(text)
    var cursor = 0
    while (cursor < text.length) {
        val found = hay.indexOf(needle, cursor)
        if (found < 0) {
            withStyle(SpanStyle(color = base)) { append(text.substring(cursor)) }
            break
        }
        if (found > cursor) {
            withStyle(SpanStyle(color = base)) { append(text.substring(cursor, found)) }
        }
        val end = found + needle.length
        withStyle(SpanStyle(color = Color.Black, background = hit, fontWeight = FontWeight.Bold)) {
            append(text.substring(found, end))
        }
        cursor = end
    }
}
