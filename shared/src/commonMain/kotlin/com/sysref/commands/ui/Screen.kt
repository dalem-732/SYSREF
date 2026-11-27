package com.sysref.commands.ui

import com.sysref.commands.categoryOrNull
import com.sysref.commands.platformOrNull

enum class Screen(val id: String) {
    BROWSE("browse"),
    SEARCH("search"),
    ;

    companion object {
        fun fromId(id: String?): Screen = entries.firstOrNull { it.id == id } ?: BROWSE
    }
}

internal data class LaunchTarget(
    val screen: Screen,
    val platformId: String?,
    val categoryId: String?,
)

internal fun launchTarget(screen: String?, platform: String?, category: String?): LaunchTarget {
    val platformId = platformOrNull(platform)?.id
    val categoryId = if (platformId != null) categoryOrNull(category)?.id else null
    return LaunchTarget(Screen.fromId(screen), platformId, categoryId)
}
