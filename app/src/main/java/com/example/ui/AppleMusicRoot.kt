package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SongRepository
import com.example.player.MusicPlayerController
import com.example.ui.components.ExpandedPlayer
import com.example.ui.components.MiniPlayer
import com.example.ui.tabs.LibraryScreen
import com.example.ui.tabs.ListenNowScreen
import com.example.ui.tabs.RadioScreen
import com.example.ui.tabs.SearchScreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    LISTEN_NOW("Listen Now", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    RADIO("Radio", Icons.Filled.Radio, Icons.Outlined.Radio),
    LIBRARY("Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search)
}

@Composable
fun AppleMusicRoot(
    controller: MusicPlayerController,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val currentSong by controller.currentSong.collectAsState()
    val isPlaying by controller.isPlaying.collectAsState()
    val currentPositionMs by controller.currentPositionMs.collectAsState()
    val durationMs by controller.durationMs.collectAsState()
    val volume by controller.volume.collectAsState()
    val isShuffle by controller.isShuffle.collectAsState()
    val isRepeat by controller.isRepeat.collectAsState()
    val likedSet by controller.isLiked.collectAsState()
    val queue by controller.songQueue.collectAsState()

    var selectedTab by remember { mutableStateOf(AppTab.LISTEN_NOW) }

    // Expanded sheet state: progress from 0f (collapsed) to 1f (fully expanded)
    val sheetProgress = remember { Animatable(0f) }
    var isSheetExpanded by remember { mutableStateOf(false) }

    val safeDuration = if (durationMs > 0) durationMs else 30000
    val progressFraction = (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)

    // Handle system back button to collapse sheet if expanded
    BackHandler(enabled = isSheetExpanded || sheetProgress.value > 0.05f) {
        coroutineScope.launch {
            sheetProgress.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            isSheetExpanded = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Dark background visible during sheet scale-down
    ) {
        val screenHeightPx = constraints.maxHeight.toFloat()

        // Dynamic background scale & corner radius based on sheet expansion progress
        // Defer sheetProgress.value read to graphicsLayer lambda to prevent top-level recompositions on low-end devices
        val cornerShape36 = remember { RoundedCornerShape(36.dp) }

        // --- LAYER 1: Background Main Content (Tabs + Mini Player + Bottom Bar) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val currentProgress = sheetProgress.value.coerceIn(0f, 1f)
                    val bgScale = 1.0f - (0.08f * currentProgress)
                    val bgTranslationY = (1.0f - bgScale) * -120f
                    val bgCornerRadius = (36f * currentProgress).dp

                    scaleX = bgScale
                    scaleY = bgScale
                    translationY = bgTranslationY
                    clip = currentProgress > 0.01f
                    shape = if (currentProgress > 0.01f) RoundedCornerShape(bgCornerRadius) else cornerShape36
                }
                .background(Color(0xFF0D0D0E))
        ) {
            // Main Tab Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp)
            ) {
                when (selectedTab) {
                    AppTab.LISTEN_NOW -> {
                        ListenNowScreen(
                            songs = SongRepository.songs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = { song ->
                                controller.playSong(song)
                            },
                            onPlayAllClick = {
                                SongRepository.songs.firstOrNull()?.let { controller.playSong(it) }
                            },
                            onShuffleClick = {
                                if (!isShuffle) controller.toggleShuffle()
                                val random = SongRepository.songs.randomOrNull() ?: SongRepository.songs.first()
                                controller.playSong(random)
                            }
                        )
                    }

                    AppTab.RADIO -> {
                        RadioScreen(
                            onPlayStation = { song ->
                                controller.playSong(song)
                            },
                            songs = SongRepository.songs
                        )
                    }

                    AppTab.LIBRARY -> {
                        LibraryScreen(
                            songs = SongRepository.songs,
                            onSongClick = { song ->
                                controller.playSong(song)
                            }
                        )
                    }

                    AppTab.SEARCH -> {
                        SearchScreen(
                            songs = SongRepository.songs,
                            onSongClick = { song ->
                                controller.playSong(song)
                            }
                        )
                    }
                }
            }

            // Dark scrim overlay when sheet is expanding (reading sheetProgress inside graphicsLayer to avoid recomposition)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val currentProgress = sheetProgress.value.coerceIn(0f, 1f)
                        alpha = currentProgress * 0.45f
                    }
                    .background(Color.Black)
                    .clickable(enabled = isSheetExpanded) {
                        coroutineScope.launch {
                            sheetProgress.animateTo(
                                0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            isSheetExpanded = false
                        }
                    }
            )

            // Bottom Navigation Bar & Mini Player Container
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // Mini Player docked above NavigationBar
                if (currentSong != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .graphicsLayer {
                                val currentProgress = sheetProgress.value.coerceIn(0f, 1f)
                                val miniPlayerAlpha = (1f - (currentProgress * 2.5f)).coerceIn(0f, 1f)
                                alpha = miniPlayerAlpha
                                translationY = (1f - miniPlayerAlpha) * 20f
                            }
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    if (delta < 0) {
                                        coroutineScope.launch {
                                            val next = (sheetProgress.value + (-delta / screenHeightPx)).coerceIn(0f, 1f)
                                            sheetProgress.snapTo(next)
                                        }
                                    }
                                },
                                onDragStopped = { velocity ->
                                    coroutineScope.launch {
                                        if (velocity < -800f || sheetProgress.value > 0.2f) {
                                            sheetProgress.animateTo(
                                                1f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                            isSheetExpanded = true
                                        } else {
                                            sheetProgress.animateTo(
                                                0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                            isSheetExpanded = false
                                        }
                                    }
                                }
                            )
                    ) {
                        MiniPlayer(
                            song = currentSong,
                            isPlaying = isPlaying,
                            progressFraction = progressFraction,
                            onExpandClick = {
                                coroutineScope.launch {
                                    sheetProgress.animateTo(
                                        1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                    isSheetExpanded = true
                                }
                            },
                            onPlayPauseClick = { controller.togglePlayPause() },
                            onNextClick = { controller.playNext() }
                        )
                    }
                }

                // Apple Music Translucent Bottom Navigation Bar
                Surface(
                    color = Color(0xFF161618).copy(alpha = 0.94f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(58.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = tab }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) Color(0xFFFA2D48) else Color(0xFF8E8E93),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFFFA2D48) else Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- LAYER 2: Expanding Full Screen Player Sheet ---
        if (currentSong != null && (isSheetExpanded || sheetProgress.value > 0.001f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val sheetTranslationY = (1f - sheetProgress.value) * screenHeightPx
                        IntOffset(0, sheetTranslationY.roundToInt())
                    }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            coroutineScope.launch {
                                val deltaRatio = delta / screenHeightPx
                                val next = (sheetProgress.value - deltaRatio).coerceIn(0f, 1f)
                                sheetProgress.snapTo(next)
                            }
                        },
                        onDragStopped = { velocity ->
                            coroutineScope.launch {
                                val shouldDismiss = velocity > 800f || sheetProgress.value < 0.72f
                                if (shouldDismiss) {
                                    sheetProgress.animateTo(
                                        0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                    isSheetExpanded = false
                                } else {
                                    sheetProgress.animateTo(
                                        1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                    isSheetExpanded = true
                                }
                            }
                        }
                    )
            ) {
                ExpandedPlayer(
                    song = currentSong!!,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    volume = volume,
                    isShuffle = isShuffle,
                    isRepeat = isRepeat,
                    isLiked = likedSet.contains(currentSong!!.id),
                    queue = queue,
                    onPlayPauseClick = { controller.togglePlayPause() },
                    onPreviousClick = { controller.playPrevious() },
                    onNextClick = { controller.playNext() },
                    onSeekTo = { pos -> controller.seekTo(pos) },
                    onVolumeChange = { vol -> controller.setVolume(vol) },
                    onToggleShuffle = { controller.toggleShuffle() },
                    onToggleRepeat = { controller.toggleRepeat() },
                    onToggleLike = { controller.toggleLike(currentSong!!.id) },
                    onSelectQueueSong = { song -> controller.playSong(song) },
                    onDismissRequest = {
                        coroutineScope.launch {
                            sheetProgress.animateTo(
                                0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            isSheetExpanded = false
                        }
                    }
                )
            }
        }
    }
}
