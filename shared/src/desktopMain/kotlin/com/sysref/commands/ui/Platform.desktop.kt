package com.sysref.commands.ui

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system back on desktop: Esc (AppShortcut.Escape) does the same job.
}
