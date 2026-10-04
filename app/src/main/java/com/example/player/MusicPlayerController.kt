package com.example.player

import android.content.Context
import com.example.data.model.Song
import kotlinx.coroutines.flow.StateFlow

class MusicPlayerController(private val context: Context) {

    val currentSong: StateFlow<Song?> = PlaybackStateManager.currentSong
    val isPlaying: StateFlow<Boolean> = PlaybackStateManager.isPlaying
    val isBuffering: StateFlow<Boolean> = PlaybackStateManager.isBuffering
    val currentPositionMs: StateFlow<Int> = PlaybackStateManager.currentPositionMs
    val durationMs: StateFlow<Int> = PlaybackStateManager.durationMs
    val volume: StateFlow<Float> = PlaybackStateManager.volume
    val isShuffle: StateFlow<Boolean> = PlaybackStateManager.isShuffle
    val isRepeat: StateFlow<Boolean> = PlaybackStateManager.isRepeat
    val isLiked: StateFlow<Set<Int>> = PlaybackStateManager.isLiked
    val songQueue: StateFlow<List<Song>> = PlaybackStateManager.songQueue

    fun playSong(song: Song) {
        MusicPlaybackService.startService(context, MusicPlaybackService.ACTION_PLAY_SONG) { intent ->
            intent.putExtra(MusicPlaybackService.EXTRA_SONG_ID, song.id)
        }
    }

    fun togglePlayPause() {
        MusicPlaybackService.startService(context, MusicPlaybackService.ACTION_TOGGLE_PLAY_PAUSE)
    }

    fun playNext() {
        MusicPlaybackService.startService(context, MusicPlaybackService.ACTION_NEXT)
    }

    fun playPrevious() {
        MusicPlaybackService.startService(context, MusicPlaybackService.ACTION_PREVIOUS)
    }

    fun seekTo(positionMs: Int) {
        MusicPlaybackService.startService(context, MusicPlaybackService.ACTION_SEEK_TO) { intent ->
            intent.putExtra(MusicPlaybackService.EXTRA_SEEK_MS, positionMs)
        }
    }

    fun setVolume(vol: Float) {
        MusicPlaybackService.startService(context, MusicPlaybackService.ACTION_SET_VOLUME) { intent ->
            intent.putExtra(MusicPlaybackService.EXTRA_VOLUME, vol)
        }
    }

    fun toggleShuffle() {
        PlaybackStateManager.toggleShuffle()
    }

    fun toggleRepeat() {
        PlaybackStateManager.toggleRepeat()
    }

    fun toggleLike(songId: Int) {
        PlaybackStateManager.toggleLike(songId)
    }

    fun stopPlayback() {
        MusicPlaybackService.startService(context, MusicPlaybackService.ACTION_STOP)
    }

    fun release() {
        // In Spotify/Apple Music, activity release does not kill playback if playing.
        // It remains in foreground service until stopped by user.
    }
}
