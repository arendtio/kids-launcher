package com.kidspace.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentGateChallengeTest {
    @Test
    fun generate_producesMatchingDigits() {
        val challenge = ParentGateChallenge.generate(length = 3)
        assertEquals(3, challenge.expectedDigits.length)
        assertTrue(challenge.prompt.isNotBlank())
    }

    @Test
    fun verify_acceptsCorrectInput() {
        val challenge = ParentGateChallenge.Challenge("one, two", "12")
        assertTrue(ParentGateChallenge.verify(challenge, "12"))
    }

    @Test
    fun verify_rejectsIncorrectInput() {
        val challenge = ParentGateChallenge.Challenge("one, two", "12")
        assertFalse(ParentGateChallenge.verify(challenge, "21"))
    }
}
