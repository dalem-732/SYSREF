package com.sysref.commands

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
    filter: SearchFilter,
    favorites: Set<String>,
    favoritesOnly: Boolean,
): List<CommandEntry> {
    if (!favoritesOnly) return searchCommands(commands, query, filter)
    val pool = commands.filter { it.id in favorites }
    if (query.isBlank()) return pool.filter { inFilter(it, filter) }.sortedBy { it.index }
    return searchCommands(pool, query, filter)
}

/**
 * Where favorites are kept. Android uses app-private SharedPreferences,
 * desktop uses a small JSON file in the user's config directory.
 */
interface FavoritesStorage {
    fun load(): Set<String>
    fun save(favorites: Set<String>)
}

/** In-memory storage for previews and tests. */
class MemoryFavoritesStorage(initial: Set<String> = emptySet()) : FavoritesStorage {
    private var favorites = initial
    override fun load(): Set<String> = favorites
    override fun save(favorites: Set<String>) {
        this.favorites = favorites.toSet()
    }
}
