package com.sysref.commands.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sysref.commands.Category
import com.sysref.commands.CommandEntry
import com.sysref.commands.browsePlatforms
import com.sysref.commands.categoryById
import com.sysref.commands.inBrowse
import com.sysref.commands.platformById

@Composable
internal fun PlatformPane(
    commands: List<CommandEntry>,
    favoritesCount: Int,
    onOpen: (String) -> Unit,
    onOpenFavorites: () -> Unit,
) {
    val counts = remember(commands) {
        browsePlatforms.associate { platform -> platform.id to commands.count { inBrowse(it, platform.id) } }
    }
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
                    Text("${counts[platform.id] ?: 0}", color = Amber, fontFamily = Mono, fontSize = 13.sp)
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
internal fun CategoryPane(
    commands: List<CommandEntry>,
    platformId: String,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val platform = platformById(platformId)
    val scoped = remember(commands, platformId) { commands.filter { inBrowse(it, platformId) } }
    val counts = remember(scoped) { scoped.groupingBy { it.category }.eachCount() }
    val categories = remember(counts) { Category.entries.filter { it.id in counts } }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            BackLine("< назад", onBack)
            PathLine(platform.title)
            Hint("// раздел")
        }
        items(categories, key = { it.id }) { category ->
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
                Text("${counts[category.id] ?: 0}", color = Amber, fontFamily = Mono, fontSize = 13.sp)
            }
            Rule()
        }
    }
}

@Composable
internal fun CommandPane(
    commands: List<CommandEntry>,
    platformId: String,
    categoryId: String,
    onBack: () -> Unit,
    favorites: Set<String>,
    onToggleFavorite: (CommandEntry) -> Unit,
    onCopy: (CommandEntry) -> Unit,
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
        items(rows, key = { it.id }) { entry ->
            CommandBlock(
                entry = entry,
                query = "",
                onCopy = onCopy,
                favorite = entry.id in favorites,
                onToggleFavorite = { onToggleFavorite(entry) },
            )
            Rule()
        }
    }
}
