package com.music.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.music.player.R

class PlaybackService : android.app.Service() {

    lateinit var exoPlayer: ExoPlayer
        private set

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var notificationManager: PlayerNotificationManager

    private var songTitle: String = ""
    private var songArtist: String = ""

    var onPlaybackStateChanged: ((isPlaying: Boolean) -> Unit)? = null
    var onPlaybackEnded: (() -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        exoPlayer = ExoPlayer.Builder(this).build()

        mediaSession = MediaSessionCompat(this, "MusicPlayerService").apply {
            isActive = true
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)

        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence = songTitle
                override fun createCurrentContentIntent(player: Player): PendingIntent? = null
                override fun getCurrentContentText(player: Player): CharSequence = songArtist
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): android.graphics.Bitmap? = null
            })
            .build()

        notificationManager.setPlayer(exoPlayer)
        notificationManager.setMediaSessionToken(mediaSession.sessionToken)
        notificationManager.setUseStopAction(true)

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        onPlaybackEnded?.invoke()
                    }
                    Player.STATE_READY -> {
                        onPlaybackStateChanged?.invoke(exoPlayer.isPlaying)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onPlaybackStateChanged?.invoke(isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlaybackEnded?.invoke()
            }
        })
    }

    fun prepareAndPlay(url: String, title: String, artist: String, coverUrl: String) {
        songTitle = title
        songArtist = artist

        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        updateMediaMetadata(title, artist)
    }

    private fun updateMediaMetadata(title: String, artist: String) {
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .build()
        )
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    val currentPosition: Long get() = exoPlayer.currentPosition
    val duration: Long get() = exoPlayer.duration
    val isPlaying: Boolean get() = exoPlayer.isPlaying

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "音乐播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "音乐播放通知"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification() = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_headphones)
        .setContentTitle("网易云音乐")
        .setContentText("正在播放")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        mediaSession.release()
    }

    inner class LocalBinder : android.os.Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?) = binder

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "music_playback"
        const val NOTIFICATION_ID = 1
    }
}
