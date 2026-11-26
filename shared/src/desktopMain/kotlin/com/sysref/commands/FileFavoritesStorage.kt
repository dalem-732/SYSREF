package com.sysref.commands

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Favorites as a JSON array of command ids in a per-user file:
 * Windows: %APPDATA%\SYSREF\favorites.json
 * Linux:   $XDG_CONFIG_HOME/sysref/favorites.json (default ~/.config/sysref/favorites.json)
 */
class FileFavoritesStorage(private val file: File = defaultFavoritesFile()) : FavoritesStorage {
    private val serializer = ListSerializer(String.serializer())
    private val json = Json { prettyPrint = true }

    override fun load(): Set<String> = try {
        if (file.isFile) json.decodeFromString(serializer, file.readText()).toSet() else emptySet()
    } catch (error: Exception) {
        // A broken file must not stop the app from starting.
        emptySet()
    }

    override fun save(favorites: Set<String>) {
        file.parentFile?.mkdirs()
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(json.encodeToString(serializer, favorites.sorted()))
        if (!tmp.renameTo(file)) {
            file.delete()
            tmp.renameTo(file)
        }
    }
}

fun defaultFavoritesFile(
    osName: String = System.getProperty("os.name").orEmpty(),
    env: (String) -> String? = System::getenv,
    home: String = System.getProperty("user.home").orEmpty(),
): File {
    val dir = if (osName.startsWith("Windows", ignoreCase = true)) {
        File(env("APPDATA")?.takeIf { it.isNotBlank() } ?: "$home\\AppData\\Roaming", "SYSREF")
    } else {
        File(env("XDG_CONFIG_HOME")?.takeIf { it.isNotBlank() } ?: "$home/.config", "sysref")
    }
    return File(dir, "favorites.json")
}
