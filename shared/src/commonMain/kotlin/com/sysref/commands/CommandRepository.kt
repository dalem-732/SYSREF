package com.sysref.commands

import com.sysref.commands.resources.Res
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
        )
    }

/** Reads the bundled command database (composeResources/files/commands.json). */
@OptIn(ExperimentalResourceApi::class)
suspend fun loadCommands(): List<CommandEntry> =
    parseCommands(Res.readBytes("files/commands.json").decodeToString())
