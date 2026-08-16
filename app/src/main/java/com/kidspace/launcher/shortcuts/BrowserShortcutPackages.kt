package com.kidspace.launcher.shortcuts

object BrowserShortcutPackages {
    val KNOWN_BROWSERS: List<Pair<String, String>> = listOf(
        "com.android.chrome" to "Chrome",
        "com.chrome.beta" to "Chrome Beta",
        "com.chrome.dev" to "Chrome Dev",
        "com.google.android.apps.chrome" to "Chrome",
        "org.mozilla.firefox" to "Firefox",
        "org.mozilla.firefox_beta" to "Firefox Beta",
        "org.mozilla.fenix" to "Firefox",
        "com.sec.android.app.sbrowser" to "Samsung Internet",
        "com.microsoft.emmx" to "Edge",
        "com.brave.browser" to "Brave",
    )

    private val labelByPackage = KNOWN_BROWSERS.toMap()

    fun labelFor(packageName: String?): String? = packageName?.let { labelByPackage[it] }

    val packageNames: List<String> = KNOWN_BROWSERS.map { it.first }
}
