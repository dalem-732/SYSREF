package com.sysref.commands

import com.sysref.commands.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Serializable
private data class CommandDb(val commands: List<CommandJson>)

@Serializable
private data class CommandJson(
    val command: String,
    val description: String,
    val platform: String,
    val category: String,
    val danger: Boolean = false,
)

private val json = Json { ignoreUnknownKeys = true }

fun parseCommands(text: String): List<CommandEntry> =
    json.decodeFromString<CommandDb>(text).commands.mapIndexed { index, item ->
        CommandEntry(
            command = item.command,
            description = item.description,
            platform = item.platform,
            category = item.category,
            index = index,
            danger = item.danger,
        )
    }

@OptIn(ExperimentalResourceApi::class)
suspend fun loadCommands(): List<CommandEntry> {
    val bytes = Res.readBytes("files/commands.json")
    return withContext(Dispatchers.Default) { parseCommands(bytes.decodeToString()) }
}
