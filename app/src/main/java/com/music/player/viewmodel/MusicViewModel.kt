package com.music.player.viewmodel

import android.app.Application
import android.content.*
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.music.player.data.model.Song
import com.music.player.data.repository.MusicRepository
import com.music.player.service.PlaybackService
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MusicViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main

    private val repository = MusicRepository()
    private val handler = Handler(Looper.getMainLooper())

    // Playback state
    private val _currentPlaylist = MutableLiveData<List<Song>>(emptyList())
    val currentPlaylist: LiveData<List<Song>> = _currentPlaylist

    private val _currentIndex = MutableLiveData(-1)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _loopMode = MutableLiveData(LoopMode.ORDER)
    val loopMode: LiveData<LoopMode> = _loopMode

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _currentLyrics = MutableLiveData<List<LyricLine>>(emptyList())
    val currentLyrics: LiveData<List<LyricLine>> = _currentLyrics

    private val _currentProgress = MutableLiveData(Pair(0L, 0L))
    val currentProgress: LiveData<Pair<Long, Long>> = _currentProgress

    private val _isFavorited = MutableLiveData(false)
    val isFavorited: LiveData<Boolean> = _isFavorited

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var playbackService: PlaybackService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? PlaybackService.LocalBinder ?: return
            playbackService = binder.getService()
            isBound = true
            setupServiceCallbacks()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            isBound = false
        }
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            playbackService?.let { svc ->
                if (svc.isPlaying) {
                    _currentProgress.postValue(Pair(svc.currentPosition, svc.duration))
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    fun bindService() {
        if (isBound) return
        val context = getApplication<Application>()
        val intent = Intent(context, PlaybackService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        handler.post(progressRunnable)
    }

    fun unbindService() {
        handler.removeCallbacks(progressRunnable)
        if (isBound) {
            val context = getApplication<Application>()
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun setupServiceCallbacks() {
        playbackService?.onPlaybackStateChanged = { playing ->
            _isPlaying.postValue(playing)
        }
        playbackService?.onPlaybackEnded = {
            playNext()
        }
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int) {
        _currentPlaylist.value = songs
        _currentIndex.value = startIndex
        playSongAtIndex(startIndex)
    }

    fun playSong(song: Song) {
        val current = _currentPlaylist.value?.toMutableList() ?: mutableListOf()
        val idx = current.indexOfFirst { it.id == song.id }
        if (idx >= 0) {
            _currentIndex.value = idx
            playSongAtIndex(idx)
        } else {
            current.add(song)
            _currentPlaylist.value = current
            _currentIndex.value = current.size - 1
            playSongAtIndex(current.size - 1)
        }
    }

    private fun playSongAtIndex(index: Int, attempt: Int = 0) {
        val songs = _currentPlaylist.value ?: return
        if (index < 0 || index >= songs.size) return
        if (attempt > 5) {
            Toast.makeText(getApplication(), "当前歌曲无版权", Toast.LENGTH_SHORT).show()
            return
        }

        val song = songs[index]
        _currentSong.value = song
        loadLyrics(song.id)

        launch {
            val urlResult = repository.fetchSongUrl(song.id)
            urlResult.fold(
                onSuccess = { url ->
                    if (url.isNullOrEmpty()) {
                        Toast.makeText(getApplication(), "当前歌曲无版权", Toast.LENGTH_SHORT).show()
                        playNextFromCurrent()
                    } else {
                        playbackService?.prepareAndPlay(
                            url, song.name, song.artistNames, song.album.picUrl
                        )
                        _isPlaying.value = true
                    }
                },
                onFailure = {
                    playSongAtIndex(index, attempt + 1)
                }
            )
        }
    }

    fun togglePlayPause() {
        playbackService?.togglePlayPause()
    }

    fun playNext() {
        val mode = _loopMode.value ?: LoopMode.ORDER
        when (mode) {
            LoopMode.ORDER -> playNextFromCurrent()
            LoopMode.LOOP -> {
                val idx = _currentIndex.value ?: return
                playSongAtIndex(idx)
            }
            LoopMode.RANDOM -> {
                val songs = _currentPlaylist.value ?: return
                if (songs.size <= 1) return
                val randomIdx = (0 until songs.size).random()
                _currentIndex.value = randomIdx
                playSongAtIndex(randomIdx)
            }
        }
    }

    fun playPrevious() {
        val songs = _currentPlaylist.value ?: return
        val current = _currentIndex.value ?: return
        val prevIdx = if (current > 0) current - 1 else songs.size - 1
        _currentIndex.value = prevIdx
        playSongAtIndex(prevIdx)
    }

    private fun playNextFromCurrent() {
        val songs = _currentPlaylist.value ?: return
        val current = _currentIndex.value ?: return
        val nextIdx = if (current < songs.size - 1) current + 1 else 0
        _currentIndex.value = nextIdx
        playSongAtIndex(nextIdx)
    }

    fun cycleLoopMode() {
        val current = _loopMode.value ?: LoopMode.ORDER
        _loopMode.value = when (current) {
            LoopMode.ORDER -> LoopMode.LOOP
            LoopMode.LOOP -> LoopMode.RANDOM
            LoopMode.RANDOM -> LoopMode.ORDER
        }
    }

    fun toggleFavorite() {
        _isFavorited.value = !(_isFavorited.value ?: false)
    }

    fun seekTo(positionMs: Long) {
        playbackService?.seekTo(positionMs)
    }

    private fun loadLyrics(songId: Long) {
        launch {
            val result = repository.fetchLyric(songId)
            result.fold(
                onSuccess = { raw ->
                    _currentLyrics.value = if (raw.isNullOrBlank()) emptyList() else parseLrc(raw)
                },
                onFailure = { _currentLyrics.value = emptyList() }
            )
        }
    }

    private fun parseLrc(raw: String): List<LyricLine> {
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
        return raw.lines().mapNotNull { line ->
            val match = regex.find(line) ?: return@mapNotNull null
            val min = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
            val sec = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
            val ms = match.groupValues[3].let {
                if (it.length == 2) it.toLong() * 10 else it.toLongOrNull() ?: 0L
            }
            val text = match.groupValues[4].trim()
            if (text.isEmpty()) null else LyricLine(min * 60_000 + sec * 1_000 + ms, text)
        }.sortedBy { it.timeMs }
    }

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
        handler.removeCallbacks(progressRunnable)
    }
}
