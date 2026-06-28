package com.music.player.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.music.player.data.model.Playlist
import com.music.player.data.model.Song
import com.music.player.data.repository.MusicRepository
import com.music.player.databinding.ActivityPlaylistDetailBinding
import com.music.player.ui.adapter.SongAdapter
import com.music.player.viewmodel.MusicViewModel
import kotlinx.coroutines.*

class PlaylistDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistDetailBinding
    private lateinit var viewModel: MusicViewModel
    private val repository = MusicRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener { finish() }

        val playlistId = intent.getLongExtra("playlist_id", 0)
        if (playlistId > 0) loadPlaylistDetail(playlistId)
    }

    private fun loadPlaylistDetail(id: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = repository.fetchPlaylistDetail(id)
            result.fold(
                onSuccess = { playlist ->
                    displayPlaylist(playlist)
                },
                onFailure = { }
            )
        }
    }

    private fun displayPlaylist(playlist: Playlist) {
        binding.textName.text = playlist.name
        binding.textCreator.text = playlist.creator?.nickname ?: ""
        binding.textDescription.text = playlist.description ?: ""
        Glide.with(this).load(playlist.coverImgUrl).into(binding.imageCover)

        val songs = playlist.tracks ?: emptyList()
        val adapter = SongAdapter(songs, viewModel) { index ->
            viewModel.playPlaylist(songs, index)
        }
        binding.recyclerSongs.layoutManager = LinearLayoutManager(this)
        binding.recyclerSongs.adapter = adapter

        viewModel.currentSong.observe(this) { currentSong ->
            adapter.setCurrentSongId(currentSong?.id)
        }
    }
}
