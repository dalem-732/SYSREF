package com.sysref.commands

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesTest {
    private val commands = listOf(
        entry(0, "apt update", "Обновить списки пакетов.", "debian", "packages"),
        entry(1, "apt upgrade", "Установить доступные обновления пакетов.", "debian", "packages"),
        entry(2, "dnf upgrade", "Обновить установленные пакеты.", "rhel", "packages"),
        entry(3, "nmap -sV", "Определить версии сервисов на открытых портах.", "linux", "nettools"),
        entry(4, "Get-NetFirewallRule", "Список правил брандмауэра Windows.", "ps", "firewall"),
        entry(5, "last", "Последние входы.", "linux", "users"),
        entry(6, "last", "Последние входы (журнал).", "linux", "logs"),
    )

    private fun ids(vararg commandsByIndex: Int) = commandsByIndex.map { commands[it].id }.toSet()

    @Test
    fun idDoesNotDependOnIndex() {
        val moved = commands[1].copy(index = 99)
        assertEquals(commands[1].id, moved.id)
    }

    @Test
    fun sameCommandInDifferentCategoriesHasDifferentIds() {
        assertNotEquals(commands[5].id, commands[6].id)
    }

    @Test
    fun toggleAddsThenRemoves() {
        val id = commands[0].id
        val added = toggleFavorite(emptySet(), id)
        assertEquals(setOf(id), added)
        assertEquals(emptySet<String>(), toggleFavorite(added, id))
    }

    @Test
    fun toggleKeepsOtherFavorites() {
        val start = ids(0, 3)
        assertEquals(ids(0, 3, 4), toggleFavorite(start, commands[4].id))
        assertEquals(ids(3), toggleFavorite(start, commands[0].id))
    }

    @Test
    fun withoutChipBehavesLikePlainSearch() {
        val favorites = ids(0)
        assertEquals(
            searchCommands(commands, "upgrade", "all"),
            filterCommands(commands, "upgrade", "all", favorites, favoritesOnly = false),
        )
        assertTrue(filterCommands(commands, "", "all", favorites, favoritesOnly = false).isEmpty())
    }

    @Test
    fun emptyQueryListsAllFavoritesInDatabaseOrder() {
        val found = filterCommands(commands, "  ", "all", ids(4, 0, 3), favoritesOnly = true)
        assertEquals(listOf(0, 3, 4), found.map { it.index })
    }

    @Test
    fun searchAndFavoritesCombine() {
        // "upgrade" matches apt upgrade and dnf upgrade; only dnf upgrade is starred.
        val found = filterCommands(commands, "upgrade", "all", ids(2, 3), favoritesOnly = true)
        assertEquals(listOf("dnf upgrade"), found.map { it.command })
    }

    @Test
    fun searchFavoritesAndPlatformCombine() {
        val favorites = ids(1, 2, 3)
        val debian = filterCommands(commands, "upgrade", "debian", favorites, favoritesOnly = true)
        assertEquals(listOf("apt upgrade"), debian.map { it.command })
        val rhelAll = filterCommands(commands, "", "rhel", favorites, favoritesOnly = true)
        // rhel filter includes common linux commands
        assertEquals(listOf("dnf upgrade", "nmap -sV"), rhelAll.map { it.command })
    }

    @Test
    fun favoritesKeepSearchRanking() {
        val favorites = ids(0, 1, 2)
        val found = filterCommands(commands, "apt", "all", favorites, favoritesOnly = true)
        assertEquals(listOf("apt update", "apt upgrade"), found.map { it.command })
    }

    @Test
    fun noFavoritesGivesEmptyResult() {
        assertTrue(filterCommands(commands, "", "all", emptySet(), favoritesOnly = true).isEmpty())
        assertTrue(filterCommands(commands, "apt", "all", emptySet(), favoritesOnly = true).isEmpty())
    }

    @Test
    fun staleIdsAreIgnored() {
        val favorites = setOf("linux/nettools/removed-command", commands[3].id)
        val found = filterCommands(commands, "", "all", favorites, favoritesOnly = true)
        assertEquals(listOf("nmap -sV"), found.map { it.command })
    }

    @Test
    fun pluralCommandsRussian() {
        assertEquals("1 команда", pluralCommands(1))
        assertEquals("3 команды", pluralCommands(3))
        assertEquals("5 команд", pluralCommands(5))
        assertEquals("11 команд", pluralCommands(11))
        assertEquals("22 команды", pluralCommands(22))
    }

    private fun entry(
        index: Int,
        command: String,
        description: String,
        platform: String,
        category: String,
    ) = CommandEntry(command, description, platform, category, index)
}
