package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song

@Composable
fun LyricsView(
    song: Song,
    currentPositionMs: Int,
    durationMs: Int,
    onSeekToRatio: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val lyrics = if (song.lyrics.isNotEmpty()) song.lyrics else listOf(
        "♪ Instrumental introduction ♪",
        "Singing along with ${song.artist}",
        "Vibing to ${song.title}",
        "Feel the rhythm in the room",
        "",
        "Apple Music Spatial Audio experience",
        "Enjoying high fidelity preview stream"
    )

    val listState = rememberLazyListState()

    // Estimate current active line based on playback progress
    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val activeIndex = (progress * lyrics.size).toInt().coerceIn(0, (lyrics.size - 1).coerceAtLeast(0))

    LaunchedEffect(activeIndex) {
        if (activeIndex in lyrics.indices) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "LYRICS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(lyrics) { index, line ->
                if (line.isBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    val isActive = index == activeIndex
                    val isPast = index < activeIndex
                    val alpha = if (isActive) 1f else if (isPast) 0.65f else 0.35f
                    val scale = if (isActive) 26.sp else 22.sp
                    val weight = if (isActive) FontWeight.Bold else FontWeight.Medium

                    Text(
                        text = line,
                        fontSize = scale,
                        lineHeight = 34.sp,
                        fontWeight = weight,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .padding(vertical = 8.dp)
                            .clickable {
                                val ratio = index.toFloat() / lyrics.size
                                onSeekToRatio(ratio)
                            }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
