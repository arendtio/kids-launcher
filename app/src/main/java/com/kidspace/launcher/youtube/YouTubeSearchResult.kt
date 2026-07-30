package com.kidspace.launcher.youtube

data class YouTubeSearchResult(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val durationLabel: String,
    val watchUrl: String = "https://www.youtube.com/watch?v=$videoId",
)
