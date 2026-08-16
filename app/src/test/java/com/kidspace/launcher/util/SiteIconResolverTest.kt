package com.kidspace.launcher.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteIconResolverTest {

    private val routineManifest = """
        {
          "name": "Family Routine Coordinator",
          "icons": [
            {"src": "/pwa-192x192.png", "sizes": "192x192", "type": "image/png", "purpose": "any"},
            {"src": "/pwa-512x512.png", "sizes": "512x512", "type": "image/png", "purpose": "any"},
            {"src": "/maskable-icon-512x512.png", "sizes": "512x512", "type": "image/png", "purpose": "maskable"},
            {"src": "/app-icon.svg", "sizes": "any", "type": "image/svg+xml", "purpose": "any"}
          ]
        }
    """.trimIndent()

    @Test
    fun parseManifestIcons_prefersHighestResolutionPng() {
        val candidates = SiteIconResolver.parseManifestIcons(
            routineManifest,
            "https://routine.arendt.cloud/manifest.webmanifest",
        )
        val best = SiteIconResolver.selectBestIcon(candidates)
        assertEquals("https://routine.arendt.cloud/pwa-512x512.png", best)
    }

    @Test
    fun parseHtmlIconCandidates_findsAppleTouchIcon() {
        val html = """
            <html><head>
              <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
            </head></html>
        """.trimIndent()
        val candidates = SiteIconResolver.parseHtmlIconCandidates(html, "https://example.com/")
        val best = SiteIconResolver.selectBestIcon(candidates)
        assertEquals("https://example.com/apple-touch-icon.png", best)
    }

    @Test
    fun findManifestUrl_readsLinkTag() {
        val html = """<link rel="manifest" href="/manifest.webmanifest">"""
        assertEquals(
            "https://routine.arendt.cloud/manifest.webmanifest",
            SiteIconResolver.findManifestUrl(html, "https://routine.arendt.cloud/"),
        )
    }

    @Test
    fun parseSizeLabel_readsSquareDimensions() {
        assertEquals(512, SiteIconResolver.parseSizeLabel("512x512"))
        assertEquals(0, SiteIconResolver.parseSizeLabel("any"))
    }

    @Test
    fun resolveUrl_supportsRelativePaths() {
        assertEquals(
            "https://routine.arendt.cloud/pwa-512x512.png",
            SiteIconResolver.resolveUrl("https://routine.arendt.cloud/", "/pwa-512x512.png"),
        )
    }

    @Test
    fun selectBestIcon_prefersAnyPurposeOverMaskableAtSameSize() {
        val candidates = listOf(
            SiteIconCandidate("https://example.com/maskable.png", 512, "image/png", "maskable"),
            SiteIconCandidate("https://example.com/any.png", 512, "image/png", "any"),
        )
        assertEquals("https://example.com/any.png", SiteIconResolver.selectBestIcon(candidates))
    }

    @Test
    fun guessManifestUrls_includesCommonPaths() {
        val urls = SiteIconResolver.guessManifestUrls("https://routine.arendt.cloud")
        assertTrue(urls.contains("https://routine.arendt.cloud/manifest.webmanifest"))
    }
}
