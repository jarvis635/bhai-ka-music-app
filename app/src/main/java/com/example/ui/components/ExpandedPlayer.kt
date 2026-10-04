package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Song
import java.util.Locale

enum class ExpandedMode {
    ARTWORK,
    LYRICS,
    QUEUE
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayer(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Int,
    durationMs: Int,
    volume: Float,
    isShuffle: Boolean,
    isRepeat: Boolean,
    isLiked: Boolean,
    queue: List<Song>,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    onSelectQueueSong: (Song) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMode by remember { mutableStateOf(ExpandedMode.ARTWORK) }
    var showAudioOutputDialog by remember { mutableStateOf(false) }

    // Parse base background color
    val parsedColor = remember(song.artworkBgColor) {
        try {
            Color(android.graphics.Color.parseColor(song.artworkBgColor))
        } catch (e: Exception) {
            Color(0xFF333333)
        }
    }

    val animatedBgColor by animateColorAsState(
        targetValue = parsedColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bgColor"
    )

    // Artwork scale animation: shrinks smoothly to 0.88f when paused, expands to 1.0f when playing
    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "artworkScale"
    )

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(0f) }

    val displayPositionMs = if (isUserScrubbing) scrubPositionMs.toInt() else currentPositionMs
    val safeDuration = if (durationMs > 0) durationMs else 30000

    val backgroundBrush = remember(animatedBgColor) {
        Brush.verticalGradient(
            colors = listOf(
                animatedBgColor.copy(alpha = 0.95f),
                animatedBgColor.copy(alpha = 0.65f),
                Color(0xFF0F0F12),
                Color.Black
            )
        )
    }

    Box(
        modifier = modifier
            .testTag("expanded_player_root")
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 14.dp)
                    .width(40.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )

            // Dynamic Content Pane: Artwork / Lyrics / Queue
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (expandedMode) {
                    ExpandedMode.ARTWORK -> {
                        // Large Apple Music Album Artwork
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .aspectRatio(1f)
                                .scale(artworkScale)
                                .shadow(
                                    elevation = if (isPlaying) 28.dp else 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = Color.Black.copy(alpha = 0.6f)
                                )
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = song.artworkUrl,
                                contentDescription = "Album art for ${song.title}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    ExpandedMode.LYRICS -> {
                        LyricsView(
                            song = song,
                            currentPositionMs = displayPositionMs,
                            durationMs = safeDuration,
                            onSeekToRatio = { ratio ->
                                val targetMs = (ratio * safeDuration).toInt()
                                onSeekTo(targetMs)
                            }
                        )
                    }

                    ExpandedMode.QUEUE -> {
                        QueueView(
                            currentSong = song,
                            queue = queue,
                            isPlaying = isPlaying,
                            isShuffle = isShuffle,
                            isRepeat = isRepeat,
                            onSongSelect = onSelectQueueSong,
                            onToggleShuffle = onToggleShuffle,
                            onToggleRepeat = onToggleRepeat
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Track Title, Artist & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier.testTag("expanded_player_like")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isLiked) Color(0xFFFA2D48) else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { showAudioOutputDialog = true },
                        modifier = Modifier.testTag("expanded_player_more")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Options",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrubber Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = displayPositionMs.toFloat().coerceIn(0f, safeDuration.toFloat()),
                    onValueChange = {
                        isUserScrubbing = true
                        scrubPositionMs = it
                    },
                    onValueChangeFinished = {
                        isUserScrubbing = false
                        onSeekTo(scrubPositionMs.toInt())
                    },
                    valueRange = 0f..safeDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playback_slider")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMillis(displayPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "-${formatMillis((safeDuration - displayPositionMs).coerceAtLeast(0))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transport Controls: Prev, Play/Pause, Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .testTag("expanded_player_prev")
                        .size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onPlayPauseClick),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "expanded_play_pause"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .testTag("expanded_player_next")
                        .size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume Control Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.VolumeDown,
                    contentDescription = "Volume Down",
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp)
                )

                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White.copy(alpha = 0.85f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .testTag("volume_slider")
                )

                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = "Volume Up",
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Auxiliary Controls: Lyrics, AirPlay/Audio Output, Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lyrics button
                IconButton(
                    onClick = {
                        expandedMode = if (expandedMode == ExpandedMode.LYRICS) ExpandedMode.ARTWORK else ExpandedMode.LYRICS
                    },
                    modifier = Modifier
                        .testTag("lyrics_button")
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (expandedMode == ExpandedMode.LYRICS) Color.White.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubbleOutline,
                        contentDescription = "Lyrics",
                        tint = if (expandedMode == ExpandedMode.LYRICS) Color(0xFFFA2D48) else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // AirPlay / Audio Output
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { showAudioOutputDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = "Audio Output",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AirPods Max",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Queue button
                IconButton(
                    onClick = {
                        expandedMode = if (expandedMode == ExpandedMode.QUEUE) ExpandedMode.ARTWORK else ExpandedMode.QUEUE
                    },
                    modifier = Modifier
                        .testTag("queue_button")
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (expandedMode == ExpandedMode.QUEUE) Color.White.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatListBulleted,
                        contentDescription = "Playing Next Queue",
                        tint = if (expandedMode == ExpandedMode.QUEUE) Color(0xFFFA2D48) else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Audio Output Bottom Sheet Dialog
        if (showAudioOutputDialog) {
            ModalBottomSheet(
                onDismissRequest = { showAudioOutputDialog = false },
                containerColor = Color(0xFF1E1E20),
                sheetState = rememberModalBottomSheetState()
            ) {
                AudioOutputSheet(
                    onDismiss = { showAudioOutputDialog = false }
                )
            }
        }
    }
}

private fun formatMillis(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
