package com.sysref.commands

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopTest {
    @Test
    fun bundledDatabaseParsesAndIdsAreUnique() {
        val file = File("src/commonMain/composeResources/files/commands.json")
        val commands = parseCommands(file.readText())
        assertTrue(commands.size > 1000)
        assertEquals(commands.size, commands.map { it.id }.toSet().size)
        commands.forEach {
            platformById(it.platform)
            categoryById(it.category)
        }
        assertEquals((0 until commands.size).toList(), commands.map { it.index })
    }

    @Test
    fun fileStorageRoundTrip() {
        val dir = Files.createTempDirectory("sysref").toFile()
        val file = File(dir, "nested/favorites.json")
        val storage = FileFavoritesStorage(file)
        assertEquals(emptySet(), storage.load())
        val ids = setOf("linux/nettools/nmap -A <хост>", "debian/packages/apt update")
        storage.save(ids)
        assertEquals(ids, FileFavoritesStorage(file).load())
        storage.save(emptySet())
        assertEquals(emptySet(), FileFavoritesStorage(file).load())
        dir.deleteRecursively()
    }

    @Test
    fun brokenFileGivesEmptyFavorites() {
        val file = File.createTempFile("sysref", ".json")
        file.writeText("{not json")
        assertEquals(emptySet(), FileFavoritesStorage(file).load())
        file.delete()
    }

    @Test
    fun favoritesFileLocation() {
        val win = defaultFavoritesFile("Windows 11", { if (it == "APPDATA") "C:\\Users\\a\\AppData\\Roaming" else null }, "C:\\Users\\a")
        assertTrue(win.path.endsWith("favorites.json") && win.path.contains("SYSREF"))
        val linux = defaultFavoritesFile("Linux", { null }, "/home/a")
        assertEquals("/home/a/.config/sysref/favorites.json", linux.path)
        val xdg = defaultFavoritesFile("Linux", { if (it == "XDG_CONFIG_HOME") "/tmp/cfg" else null }, "/home/a")
        assertEquals("/tmp/cfg/sysref/favorites.json", xdg.path)
    }
}
