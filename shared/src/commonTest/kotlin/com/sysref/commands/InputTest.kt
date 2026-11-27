package com.sysref.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InputTest {
    @Test
    fun unknownIdsGiveNull() {
        assertNull(platformOrNull("windows95"))
        assertNull(platformOrNull(null))
        assertNull(categoryOrNull(""))
        assertEquals(Platform.DEBIAN, platformOrNull("debian"))
        assertEquals(Category.LVM, categoryOrNull("lvm"))
    }

    @Test
    fun unknownFilterFallsBackToAll() {
        assertEquals(SearchFilter.ALL, SearchFilter.fromId("nope"))
        assertEquals(SearchFilter.ALL, SearchFilter.fromId(null))
        assertEquals(SearchFilter.LINUX_ALL, SearchFilter.fromId("linall"))
    }

    @Test
    fun dangerFlagIsParsedAndOptional() {
        val text = """
            {"version": 1, "commands": [
              {"command": "ls", "description": "Список.", "platform": "linux", "category": "files"},
              {"command": "rm -rf <каталог>", "description": "Удалить.", "platform": "linux", "category": "files", "danger": true}
            ]}
        """.trimIndent()
        val commands = parseCommands(text)
        assertFalse(commands[0].danger)
        assertTrue(commands[1].danger)
        assertEquals("linux/files/rm -rf <каталог>", commands[1].id)
    }
}
