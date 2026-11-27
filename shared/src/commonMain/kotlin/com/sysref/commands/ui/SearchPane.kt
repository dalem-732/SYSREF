package com.sysref.commands.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sysref.commands.CommandEntry
import com.sysref.commands.SearchFilter
import com.sysref.commands.filterCommands
import com.sysref.commands.pluralCommands
import com.sysref.commands.pluralMatches

@Composable
internal fun SearchPane(
    commands: List<CommandEntry>,
    query: String,
    onQuery: (String) -> Unit,
    filter: SearchFilter,
    onFilter: (SearchFilter) -> Unit,
    favoritesOnly: Boolean,
    onFavoritesOnly: (Boolean) -> Unit,
    favorites: Set<String>,
    onToggleFavorite: (CommandEntry) -> Unit,
    onCopy: (CommandEntry) -> Unit,
    focusSearchTick: Int = 0,
) {
    val focus = remember { FocusRequester() }
    val trimmed = query.trim()
    val results = remember(commands, trimmed, filter, favorites, favoritesOnly) {
        filterCommands(commands, trimmed, filter, favorites, favoritesOnly)
    }
    LaunchedEffect(Unit) {
        if (query.isEmpty() && !favoritesOnly) runCatching { focus.requestFocus() }
    }
    LaunchedEffect(focusSearchTick) {
        if (focusSearchTick > 0) runCatching { focus.requestFocus() }
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
        val blank = trimmed.isEmpty()
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
                        entry = entry,
                        query = trimmed,
                        onCopy = onCopy,
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
        cursorBrush = SolidColor(Amber),
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
            }
        },
    )
}

@Composable
private fun FilterRow(
    selected: SearchFilter,
    onSelect: (SearchFilter) -> Unit,
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
        SearchFilter.entries.forEach { chip ->
            val on = chip == selected
            Text(
                text = chip.label,
                color = if (on) Color.Black else PhosphorMuted,
                fontFamily = Mono,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(if (on) Amber else Color.Transparent)
                    .border(1.dp, if (on) Amber else PhosphorDim)
                    .clickable { onSelect(chip) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}
