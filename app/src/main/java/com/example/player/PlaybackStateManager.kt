package com.example.player

import com.example.data.model.Song
import com.example.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlaybackStateManager {
    val currentSong = MutableStateFlow<Song?>(SongRepository.songs.firstOrNull())
    val isPlaying = MutableStateFlow(false)
    val isBuffering = MutableStateFlow(false)
    val currentPositionMs = MutableStateFlow(0)
    val durationMs = MutableStateFlow(30000)
    val volume = MutableStateFlow(0.85f)
    val isShuffle = MutableStateFlow(false)
    val isRepeat = MutableStateFlow(false)
    val isLiked = MutableStateFlow<Set<Int>>(setOf(0, 3))
    val songQueue = MutableStateFlow(SongRepository.songs)

    fun toggleLike(songId: Int) {
        val current = isLiked.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        isLiked.value = current
    }

    fun toggleShuffle() {
        isShuffle.value = !isShuffle.value
    }

    fun toggleRepeat() {
        isRepeat.value = !isRepeat.value
    }
}
