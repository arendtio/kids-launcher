package com.kidspace.launcher.domain

import kotlin.random.Random

object ParentGateChallenge {
    private val numberWords = listOf(
        "zero", "one", "two", "three", "four",
        "five", "six", "seven", "eight", "nine",
    )

    data class Challenge(
        val prompt: String,
        val expectedDigits: String,
    )

    fun generate(length: Int = 4, random: Random = Random.Default): Challenge {
        val digits = buildString {
            repeat(length) { append(random.nextInt(10)) }
        }
        val prompt = digits.map { numberWords[it.digitToInt()] }.joinToString(", ")
        return Challenge(prompt = prompt, expectedDigits = digits)
    }

    fun verify(challenge: Challenge, input: String): Boolean =
        input == challenge.expectedDigits
}
