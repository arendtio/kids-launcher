package com.kidspace.launcher.youtube

import org.junit.Assert.assertEquals
import org.junit.Test

class YouTubeDurationFormatterTest {

    @Test
    fun `formats minutes and seconds`() {
        assertEquals("4:13", YouTubeDurationFormatter.format("PT4M13S"))
    }

    @Test
    fun `formats hours minutes and seconds`() {
        assertEquals("1:02:03", YouTubeDurationFormatter.format("PT1H2M3S"))
    }

    @Test
    fun `formats seconds only`() {
        assertEquals("0:45", YouTubeDurationFormatter.format("PT45S"))
    }
}
