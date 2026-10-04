package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.data.model.Song
import com.example.data.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicPlaybackService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {
        const val CHANNEL_ID = "apple_music_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.applemusic.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.applemusic.ACTION_PAUSE"
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.example.applemusic.ACTION_TOGGLE"
        const val ACTION_NEXT = "com.example.applemusic.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.applemusic.ACTION_PREVIOUS"
        const val ACTION_SEEK_TO = "com.example.applemusic.ACTION_SEEK_TO"
        const val ACTION_PLAY_SONG = "com.example.applemusic.ACTION_PLAY_SONG"
        const val ACTION_SET_VOLUME = "com.example.applemusic.ACTION_SET_VOLUME"
        const val ACTION_STOP = "com.example.applemusic.ACTION_STOP"

        const val EXTRA_SONG_ID = "extra_song_id"
        const val EXTRA_SEEK_MS = "extra_seek_ms"
        const val EXTRA_VOLUME = "extra_volume"

        fun startService(context: Context, action: String, configure: ((Intent) -> Unit)? = null) {
            val intent = Intent(context, MusicPlaybackService::class.java).apply {
                this.action = action
                configure?.invoke(this)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error starting service: ${e.message}", e)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private var progressTrackerJob: Job? = null
    private var currentArtworkBitmap: Bitmap? = null
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    // Player state tracking to prevent invalid state operations
    private var isPrepared = false
    private var isPreparing = false
    private var preparedSongId: Int? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        setupMediaSession()
        setupMediaPlayer()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Apple Music persistent background playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun setupMediaSession() {
        mediaSession = MediaSession(this, "AppleMusicSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    handlePlay()
                }

                override fun onPause() {
                    handlePause()
                }

                override fun onSkipToNext() {
                    handleNext()
                }

                override fun onSkipToPrevious() {
                    handlePrevious()
                }

                override fun onSeekTo(pos: Long) {
                    handleSeekTo(pos.toInt())
                }

                override fun onStop() {
                    handleStop()
                }
            })
            isActive = true
        }
    }

    private fun reconfigurePlayer() {
        mediaPlayer?.apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            reconfigurePlayer()
            setOnPreparedListener { mp ->
                isPreparing = false
                isPrepared = true
                val song = PlaybackStateManager.currentSong.value
                preparedSongId = song?.id
                PlaybackStateManager.isBuffering.value = false

                val dur = if (mp.duration > 0) mp.duration else 30000
                PlaybackStateManager.durationMs.value = dur

                if (PlaybackStateManager.isPlaying.value) {
                    try {
                        mp.start()
                        startProgressTracker()
                        updatePlaybackState(PlaybackState.STATE_PLAYING, mp.currentPosition.toLong())
                        updateNotification(song, true)
                    } catch (e: Exception) {
                        Log.e("MusicService", "Error starting playback in onPrepared: ${e.message}")
                    }
                } else {
                    updatePlaybackState(PlaybackState.STATE_PAUSED, 0L)
                    updateNotification(song, false)
                }
            }
            setOnCompletionListener {
                if (PlaybackStateManager.isRepeat.value) {
                    handleSeekTo(0)
                    handlePlay()
                } else {
                    handleNext()
                }
            }
            setOnErrorListener { _, what, extra ->
                Log.e("MusicService", "MediaPlayer error: what=$what, extra=$extra")
                isPreparing = false
                isPrepared = false
                preparedSongId = null
                PlaybackStateManager.isBuffering.value = false
                PlaybackStateManager.isPlaying.value = false
                updatePlaybackState(PlaybackState.STATE_ERROR, 0)
                true
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        // Guarantee foreground notification is shown immediately
        val initialSong = PlaybackStateManager.currentSong.value ?: SongRepository.songs.firstOrNull()
        updateNotification(initialSong, PlaybackStateManager.isPlaying.value)

        if (action != null) {
            when (action) {
                ACTION_PLAY -> handlePlay()
                ACTION_PAUSE -> handlePause()
                ACTION_TOGGLE_PLAY_PAUSE -> {
                    if (PlaybackStateManager.isPlaying.value) {
                        handlePause()
                    } else {
                        handlePlay()
                    }
                }
                ACTION_NEXT -> handleNext()
                ACTION_PREVIOUS -> handlePrevious()
                ACTION_PLAY_SONG -> {
                    val songId = intent.getIntExtra(EXTRA_SONG_ID, -1)
                    val song = SongRepository.getSongById(songId)
                    if (song != null) {
                        playSong(song, autoPlay = true)
                    }
                }
                ACTION_SEEK_TO -> {
                    val pos = intent.getIntExtra(EXTRA_SEEK_MS, 0)
                    handleSeekTo(pos)
                }
                ACTION_SET_VOLUME -> {
                    val vol = intent.getFloatExtra(EXTRA_VOLUME, 0.85f)
                    setVolume(vol)
                }
                ACTION_STOP -> handleStop()
            }
        }

        return START_STICKY
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener(this)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> handlePause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> handlePause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaPlayer?.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                val vol = PlaybackStateManager.volume.value
                mediaPlayer?.setVolume(vol, vol)
                handlePlay()
            }
        }
    }

    private fun playSong(song: Song, autoPlay: Boolean = true) {
        if (preparedSongId == song.id && isPrepared) {
            if (autoPlay) {
                handlePlay()
            }
            return
        }

        PlaybackStateManager.currentSong.value = song
        PlaybackStateManager.currentPositionMs.value = 0
        PlaybackStateManager.isBuffering.value = true
        PlaybackStateManager.isPlaying.value = autoPlay

        isPrepared = false
        isPreparing = true
        preparedSongId = null

        loadArtworkBitmap(song)
        updateNotification(song, isPlaying = autoPlay)
        updatePlaybackState(PlaybackState.STATE_BUFFERING, 0)

        try {
            val player = mediaPlayer ?: MediaPlayer().also {
                mediaPlayer = it
                setupMediaPlayer()
            }
            player.reset()
            reconfigurePlayer()
            player.setDataSource(song.mp4Link)
            val vol = PlaybackStateManager.volume.value
            player.setVolume(vol, vol)
            player.prepareAsync()
        } catch (e: Exception) {
            Log.e("MusicService", "Error setting data source: ${e.message}", e)
            isPreparing = false
            isPrepared = false
            PlaybackStateManager.isBuffering.value = false
            PlaybackStateManager.isPlaying.value = false
        }
    }

    private fun handlePlay() {
        if (!requestAudioFocus()) return

        val current = PlaybackStateManager.currentSong.value ?: SongRepository.songs.firstOrNull() ?: return

        // If not prepared or different song, initiate clean async prepare
        if (preparedSongId != current.id || !isPrepared) {
            if (isPreparing) {
                PlaybackStateManager.isPlaying.value = true
                updateNotification(current, true)
                return
            } else {
                playSong(current, autoPlay = true)
                return
            }
        }

        // Ready to start safely
        val player = mediaPlayer ?: return
        try {
            PlaybackStateManager.isPlaying.value = true
            player.start()
            startProgressTracker()
            updatePlaybackState(PlaybackState.STATE_PLAYING, player.currentPosition.toLong())
            updateNotification(current, true)
        } catch (e: Exception) {
            Log.e("MusicService", "Error starting player: ${e.message}", e)
        }
    }

    private fun handlePause() {
        PlaybackStateManager.isPlaying.value = false
        if (isPrepared) {
            try {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error pausing player: ${e.message}")
            }
        }
        stopProgressTracker()
        updatePlaybackState(PlaybackState.STATE_PAUSED, mediaPlayer?.currentPosition?.toLong() ?: 0L)
        updateNotification(PlaybackStateManager.currentSong.value, false)
    }

    private fun handleNext() {
        val current = PlaybackStateManager.currentSong.value ?: return
        val list = PlaybackStateManager.songQueue.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        val nextSong = if (PlaybackStateManager.isShuffle.value) {
            list.filter { it.id != current.id }.randomOrNull() ?: list.first()
        } else {
            if (currentIndex != -1 && currentIndex + 1 < list.size) {
                list[currentIndex + 1]
            } else {
                list.firstOrNull() ?: current
            }
        }
        playSong(nextSong, autoPlay = true)
    }

    private fun handlePrevious() {
        val player = mediaPlayer
        if (isPrepared && player != null && player.currentPosition > 3000) {
            handleSeekTo(0)
            return
        }
        val current = PlaybackStateManager.currentSong.value ?: return
        val list = PlaybackStateManager.songQueue.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        val prevSong = if (currentIndex > 0) {
            list[currentIndex - 1]
        } else {
            list.lastOrNull() ?: current
        }
        playSong(prevSong, autoPlay = true)
    }

    private fun handleSeekTo(positionMs: Int) {
        val clamped = positionMs.coerceAtLeast(0)
        PlaybackStateManager.currentPositionMs.value = clamped
        if (isPrepared) {
            try {
                mediaPlayer?.seekTo(clamped)
                val state = if (PlaybackStateManager.isPlaying.value) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
                updatePlaybackState(state, clamped.toLong())
            } catch (e: Exception) {
                Log.e("MusicService", "Error seeking: ${e.message}")
            }
        }
    }

    private fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        PlaybackStateManager.volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            Log.e("MusicService", "Error setting volume: ${e.message}")
        }
    }

    private fun handleStop() {
        stopProgressTracker()
        PlaybackStateManager.isPlaying.value = false
        try {
            if (isPrepared && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error stopping player: ${e.message}")
        }
        isPrepared = false
        isPreparing = false
        preparedSongId = null
        updatePlaybackState(PlaybackState.STATE_STOPPED, 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = serviceScope.launch {
            while (isActive && PlaybackStateManager.isPlaying.value) {
                try {
                    if (isPrepared) {
                        mediaPlayer?.let { mp ->
                            if (mp.isPlaying) {
                                val current = mp.currentPosition
                                if (PlaybackStateManager.currentPositionMs.value != current) {
                                    PlaybackStateManager.currentPositionMs.value = current
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient exceptions during state changes
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    private fun loadArtworkBitmap(song: Song) {
        serviceScope.launch {
            try {
                val loader = ImageLoader(this@MusicPlaybackService)
                val request = ImageRequest.Builder(this@MusicPlaybackService)
                    .data(song.artworkUrl)
                    .allowHardware(false)
                    .build()
                val result = withContext(Dispatchers.IO) { loader.execute(request) }
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        currentArtworkBitmap = bitmap
                        updateMediaMetadata(song, bitmap)
                        updateNotification(song, PlaybackStateManager.isPlaying.value)
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error loading artwork: ${e.message}")
            }
        }
    }

    private fun updateMediaMetadata(song: Song, bitmap: Bitmap?) {
        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, PlaybackStateManager.durationMs.value.toLong())

        if (bitmap != null) {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap)
        }

        mediaSession?.setMetadata(metadataBuilder.build())
    }

    private fun updatePlaybackState(state: Int, positionMs: Long) {
        val actions = PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO or
                PlaybackState.ACTION_STOP

        val playbackState = PlaybackState.Builder()
            .setActions(actions)
            .setState(state, positionMs, 1.0f)
            .build()

        mediaSession?.setPlaybackState(playbackState)
    }

    private fun updateNotification(song: Song?, isPlaying: Boolean) {
        val activeSong = song ?: return
        val session = mediaSession ?: return

        try {
            val contentIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val prevPendingIntent = PendingIntent.getService(
                this,
                1,
                Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREVIOUS },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val playPausePendingIntent = PendingIntent.getService(
                this,
                2,
                Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY_PAUSE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextPendingIntent = PendingIntent.getService(
                this,
                3,
                Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val playPauseIcon = if (isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            val playPauseTitle = if (isPlaying) "Pause" else "Play"

            val mediaStyle = Notification.MediaStyle()
                .setMediaSession(session.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)

            val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
            }

            notificationBuilder
                .setStyle(mediaStyle)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(activeSong.title)
                .setContentText(activeSong.artist)
                .setSubText(activeSong.album)
                .setContentIntent(contentIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(isPlaying)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

            currentArtworkBitmap?.let {
                notificationBuilder.setLargeIcon(it)
            }

            val notification = notificationBuilder.build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error updating notification: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressTracker()
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        mediaSession?.release()
        mediaSession = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
