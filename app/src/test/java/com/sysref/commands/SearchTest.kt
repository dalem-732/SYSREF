package com.sysref.commands

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    private val commands = listOf(
        entry(0, "apt", "Установить пакет.", "debian", "packages"),
        entry(1, "apt update", "Обновить списки пакетов.", "debian", "packages"),
        entry(2, "apt upgrade", "Установить доступные обновления пакетов.", "debian", "packages"),
        entry(3, "dnf upgrade", "Обновить установленные пакеты.", "rhel", "packages"),
        entry(4, "nmap -sV", "Определить версии сервисов на открытых портах.", "linux", "nettools"),
        entry(5, "Get-NetFirewallRule", "Список правил брандмауэра Windows.", "ps", "firewall"),
    )

    @Test
    fun exactCommandBeatsPrefix() {
        val found = searchCommands(commands, "apt", "all").map { it.command }
        assertEquals("apt", found.first())
        assertTrue(found.indexOf("apt update") < found.indexOf("apt upgrade"))
    }

    @Test
    fun prefixBeatsDescription() {
        val found = searchCommands(commands, "upgrade", "all").map { it.command }
        assertEquals(listOf("apt upgrade", "dnf upgrade"), found.take(2))
    }

    @Test
    fun matchesInsideWordAndCase() {
        val found = searchCommands(commands, "GRADE", "all").map { it.command }
        assertTrue(found.contains("apt upgrade"))
    }

    @Test
    fun russianDescriptionMatch() {
        val found = searchCommands(commands, "обнов", "all").map { it.command }
        assertTrue(found.contains("apt update"))
        assertTrue(found.contains("dnf upgrade"))
    }

    @Test
    fun yoFoldsToE() {
        val withYo = listOf(entry(0, "echo", "Учётная запись.", "linux", "users"))
        assertEquals(1, searchCommands(withYo, "учет", "all").size)
    }

    @Test
    fun debianFilterIncludesCommonLinux() {
        val found = searchCommands(commands, "nmap", "debian").map { it.command }
        assertEquals(listOf("nmap -sV"), found)
    }

    @Test
    fun cmdFilterHidesPowerShell() {
        val found = searchCommands(commands, "firewall", "cmd")
        assertTrue(found.isEmpty())
    }

    @Test
    fun toolNameDoesNotPullWholeCategory() {
        val more = commands + entry(6, "tcpdump -i eth0", "Печатать пакеты.", "linux", "nettools")
        val found = searchCommands(more, "nmap", "all").map { it.command }
        assertEquals(listOf("nmap -sV"), found)
    }

    @Test
    fun platformAliasMatches() {
        val found = searchCommands(commands, "убунту", "all")
        assertTrue(found.any { it.platform == "debian" })
        assertTrue(found.none { it.platform == "rhel" })
    }

    private fun entry(
        index: Int,
        command: String,
        description: String,
        platform: String,
        category: String,
    ) = CommandEntry(command, description, platform, category, index)
}
