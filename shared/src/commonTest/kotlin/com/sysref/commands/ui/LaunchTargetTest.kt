package com.sysref.commands.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LaunchTargetTest {
    @Test
    fun validTargetIsKept() {
        assertEquals(LaunchTarget(Screen.BROWSE, "debian", "packages"), launchTarget("browse", "debian", "packages"))
        assertEquals(Screen.SEARCH, launchTarget("search", null, null).screen)
    }

    @Test
    fun invalidValuesFallBack() {
        assertEquals(LaunchTarget(Screen.BROWSE, null, null), launchTarget("x", "bogus", "packages"))
        assertEquals(LaunchTarget(Screen.BROWSE, "rhel", null), launchTarget(null, "rhel", "bogus"))
        assertEquals(LaunchTarget(Screen.BROWSE, null, null), launchTarget("", null, "packages"))
    }
}
