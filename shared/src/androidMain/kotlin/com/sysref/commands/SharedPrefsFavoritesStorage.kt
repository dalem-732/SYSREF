package com.sysref.commands

import android.content.Context

/**
 * Favorites in app-private SharedPreferences.
 * File "favorites", key "ids" — the same as in 1.1.0, so favorites survive the upgrade.
 */
class SharedPrefsFavoritesStorage(context: Context) : FavoritesStorage {
    private val prefs = context.applicationContext
        .getSharedPreferences("favorites", Context.MODE_PRIVATE)

    override fun load(): Set<String> = prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    override fun save(favorites: Set<String>) {
        prefs.edit().putStringSet(KEY, favorites.toSet()).apply()
    }

    private companion object {
        const val KEY = "ids"
    }
}
