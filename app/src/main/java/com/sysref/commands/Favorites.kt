package com.sysref.commands

import android.content.Context

fun toggleFavorite(favorites: Set<String>, id: String): Set<String> =
    if (id in favorites) favorites - id else favorites + id

/**
 * Search pane results.
 * Without the favorites chip it is plain [searchCommands]: nothing until a query is typed.
 * With the chip only favorites are searched, and an empty query lists all favorites
 * (platform filter still applies) in database order.
 */
fun filterCommands(
    commands: List<CommandEntry>,
    query: String,
    filter: String,
    favorites: Set<String>,
    favoritesOnly: Boolean,
): List<CommandEntry> {
    if (!favoritesOnly) return searchCommands(commands, query, filter)
    val pool = commands.filter { it.id in favorites }
    if (query.isBlank()) return pool.filter { inFilter(it, filter) }.sortedBy { it.index }
    return searchCommands(pool, query, filter)
}

/** Favorites live only on the device, in app-private SharedPreferences. */
class FavoritesStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun load(): Set<String> = prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    fun save(favorites: Set<String>) {
        prefs.edit().putStringSet(KEY, favorites.toSet()).apply()
    }

    private companion object {
        const val KEY = "ids"
    }
}
