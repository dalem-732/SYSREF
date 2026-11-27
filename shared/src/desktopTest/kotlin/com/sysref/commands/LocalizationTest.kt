package com.sysref.commands

import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalizationTest {
    private data class Strings(val strings: Map<String, String>, val plurals: Map<String, Map<String, String>>)

    private fun read(dir: String): Strings {
        val file = File("src/commonMain/composeResources/$dir/strings.xml")
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val strings = doc.getElementsByTagName("string").let { list ->
            (0 until list.length).map { list.item(it) as Element }.associate { it.getAttribute("name") to it.textContent }
        }
        val plurals = doc.getElementsByTagName("plurals").let { list ->
            (0 until list.length).map { list.item(it) as Element }.associate { plural ->
                val items = plural.getElementsByTagName("item")
                plural.getAttribute("name") to (0 until items.length).map { items.item(it) as Element }
                    .associate { it.getAttribute("quantity") to it.textContent }
            }
        }
        return Strings(strings, plurals)
    }

    private val placeholder = Regex("%\\d+\\$[sd]")

    private fun placeholders(text: String) = placeholder.findAll(text).map { it.value }.toSortedSet()

    @Test
    fun russianAndEnglishHaveTheSameKeys() {
        val en = read("values")
        val ru = read("values-ru")
        assertEquals(en.strings.keys, ru.strings.keys)
        assertEquals(en.plurals.keys, ru.plurals.keys)
    }

    @Test
    fun placeholdersMatch() {
        val en = read("values")
        val ru = read("values-ru")
        en.strings.forEach { (key, text) -> assertEquals(placeholders(text), placeholders(ru.strings.getValue(key)), key) }
        en.plurals.forEach { (key, items) ->
            val expected = placeholders(items.getValue("other"))
            (items.values + ru.plurals.getValue(key).values).forEach { assertEquals(expected, placeholders(it), key) }
        }
    }

    @Test
    fun pluralsCoverLanguageRules() {
        read("values").plurals.forEach { (key, items) -> assertTrue(items.keys.containsAll(setOf("one", "other")), key) }
        read("values-ru").plurals.forEach { (key, items) ->
            assertTrue(items.keys.containsAll(setOf("one", "few", "many", "other")), key)
        }
    }

    @Test
    fun everyPlatformAndCategoryHasTitles() {
        val en = read("values").strings
        Platform.entries.forEach {
            assertTrue("platform_${it.id}_title" in en && "platform_${it.id}_blurb" in en, it.id)
        }
        Category.entries.forEach { assertTrue("category_${it.id}" in en, it.id) }
        assertEquals("Пакеты", read("values-ru").strings["category_packages"])
    }
}
