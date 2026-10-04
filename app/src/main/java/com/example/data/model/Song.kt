package com.example.data.model

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val mp4Link: String,
    val artworkUrl: String,
    val artworkBgColor: String,
    val lyrics: List<String> = emptyList(),
    val album: String = "Top Hits",
    val durationSeconds: Int = 30
)
