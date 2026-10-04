package com.example

import com.example.data.repository.SongRepository
import com.example.player.PlaybackStateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSongRepositoryCatalog() {
        val songs = SongRepository.songs
        assertTrue(songs.size >= 10)
        assertNotNull(songs.find { it.title == "Espresso" })
        assertNotNull(songs.find { it.title == "Birds Of A Feather" })
    }

    @Test
    fun testPlaybackStateManagerStateTransitions() {
        assertNotNull(PlaybackStateManager.currentSong.value)
        val initialShuffle = PlaybackStateManager.isShuffle.value
        PlaybackStateManager.toggleShuffle()
        assertEquals(!initialShuffle, PlaybackStateManager.isShuffle.value)
        PlaybackStateManager.toggleShuffle()

        val initialRepeat = PlaybackStateManager.isRepeat.value
        PlaybackStateManager.toggleRepeat()
        assertEquals(!initialRepeat, PlaybackStateManager.isRepeat.value)
        PlaybackStateManager.toggleRepeat()
    }
}
