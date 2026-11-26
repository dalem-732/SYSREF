package com.sysref.commands.ui

import androidx.compose.runtime.Composable

/** System back gesture/button. Android wires it to BackHandler; desktop uses Esc instead. */
@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)

/** Keyboard shortcuts delivered by the host window (desktop). */
enum class AppShortcut {
    /** Ctrl+F: open search and focus the query field. */
    FocusSearch,

    /** Esc: clear the query, or go one level back. */
    Escape,
}
