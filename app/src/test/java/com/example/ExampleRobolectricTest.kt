package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.SongRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Apple Music", appName)
    }

    @Test
    fun `verify songs repository loaded`() {
        val songs = SongRepository.songs
        assertTrue("Song list should not be empty", songs.isNotEmpty())
        val first = songs.first()
        assertEquals("A Bar Song (Tipsy)", first.title)
        assertEquals("Shaboozey", first.artist)
        assertNotNull(first.mp4Link)
    }

    @Test
    fun `search songs by query`() {
        val results = SongRepository.searchSongs("Espresso")
        assertEquals(1, results.size)
        assertEquals("Sabrina Carpenter", results[0].artist)
    }
}
