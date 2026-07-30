package com.kidspace.launcher.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainMatcherTest {
    @Test
    fun isAllowedNavigation_allowsSameHost() {
        assertTrue(
            DomainMatcher.isAllowedNavigation(
                "https://pbskids.org/games",
                "https://pbskids.org/videos",
            ),
        )
    }

    @Test
    fun isAllowedNavigation_allowsSubdomain() {
        assertTrue(
            DomainMatcher.isAllowedNavigation(
                "https://pbskids.org",
                "https://games.pbskids.org/play",
            ),
        )
    }

    @Test
    fun isAllowedNavigation_blocksExternalDomain() {
        assertFalse(
            DomainMatcher.isAllowedNavigation(
                "https://pbskids.org",
                "https://example.com",
            ),
        )
    }
}
