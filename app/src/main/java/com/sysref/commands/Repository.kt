package com.sysref.commands

import android.content.Context
import org.json.JSONObject

object CommandRepository {
    fun load(context: Context): List<CommandEntry> {
        val text = context.assets.open("commands.json").bufferedReader().use { it.readText() }
        val array = JSONObject(text).getJSONArray("commands")
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            CommandEntry(
                command = item.getString("command"),
                description = item.getString("description"),
                platform = item.getString("platform"),
                category = item.getString("category"),
                index = index,
            )
        }
    }
}
