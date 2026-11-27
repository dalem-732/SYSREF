package com.sysref.commands

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileFavoritesStorage(private val file: File = defaultFavoritesFile()) : FavoritesStorage {
    private val serializer = ListSerializer(String.serializer())
    private val json = Json { prettyPrint = true }

    override fun load(): Set<String> = try {
        if (file.isFile) json.decodeFromString(serializer, file.readText()).toSet() else emptySet()
    } catch (error: Exception) {
        emptySet()
    }

    override fun save(favorites: Set<String>) {
        try {
            write(json.encodeToString(serializer, favorites.sorted()))
        } catch (error: IOException) {
            return
        } catch (error: SecurityException) {
            return
        }
    }

    private fun write(text: String) {
        val target = file.absoluteFile.toPath()
        val dir = target.parent
        Files.createDirectories(dir)
        val tmp = Files.createTempFile(dir, "${target.fileName}.", ".tmp")
        try {
            Files.writeString(tmp, text)
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (error: AtomicMoveNotSupportedException) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(tmp)
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
