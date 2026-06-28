package com.music.player.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.music.player.R
import com.music.player.databinding.ActivityMainBinding
import com.music.player.databinding.IncludeMiniPlayerBinding
import com.music.player.viewmodel.MusicViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var miniBinding: IncludeMiniPlayerBinding
    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        miniBinding = IncludeMiniPlayerBinding.bind(binding.root.findViewById(R.id.mini_player_root))
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        viewModel.bindService()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        setupMiniPlayer()
    }

    private fun setupMiniPlayer() {
        viewModel.currentSong.observe(this) { song ->
            song?.let {
                miniBinding.textMiniTitle.text = it.name
                miniBinding.textMiniArtist.text = it.artistNames
                Glide.with(this).load(it.album.picUrl).into(miniBinding.imageMiniCover)
            }
        }

        viewModel.isPlaying.observe(this) { playing ->
            miniBinding.btnMiniPlayPause.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        viewModel.currentProgress.observe(this) { (pos, dur) ->
            if (dur > 0) {
                miniBinding.progressBar.max = 1000
                miniBinding.progressBar.progress = (pos * 1000 / dur).toInt()
            }
        }

        miniBinding.btnMiniPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        miniBinding.btnMiniNext.setOnClickListener { viewModel.playNext() }
        miniBinding.miniPlayerRoot.setOnClickListener {
            if (viewModel.currentSong.value != null) {
                startActivity(android.content.Intent(this, PlayerActivity::class.java))
                overridePendingTransition(R.anim.slide_up_in, 0)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unbindService()
    }
}
