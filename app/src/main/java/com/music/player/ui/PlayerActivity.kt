package com.music.player.ui

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.music.player.R
import com.music.player.data.model.Song
import com.music.player.databinding.ActivityPlayerBinding
import com.music.player.ui.adapter.LyricAdapter
import com.music.player.viewmodel.LyricLine
import com.music.player.viewmodel.LoopMode
import com.music.player.viewmodel.MusicViewModel
import androidx.lifecycle.ViewModelProvider

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var viewModel: MusicViewModel
    private var cdAnimator: ObjectAnimator? = null
    private var isUserSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        binding.btnBack.setOnClickListener { finish() }

        setupObservers()
        setupControls()
    }

    private fun setupObservers() {
        viewModel.currentSong.observe(this) { song ->
            song?.let { displaySong(it) }
        }

        viewModel.isPlaying.observe(this) { playing ->
            binding.btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play
            )
            if (playing) cdAnimator?.start() else cdAnimator?.pause()
        }

        viewModel.currentProgress.observe(this) { (pos, dur) ->
            if (!isUserSeeking && dur > 0) {
                binding.seekBar.max = dur.toInt()
                binding.seekBar.progress = pos.toInt()
                binding.textCurrentTime.text = formatTime(pos)
                binding.textTotalTime.text = formatTime(dur)
            }
        }

        viewModel.currentLyrics.observe(this) { lyrics ->
            val adapter = LyricAdapter(lyrics)
            binding.recyclerLyrics.layoutManager = LinearLayoutManager(this)
            binding.recyclerLyrics.adapter = adapter

            viewModel.currentProgress.observe(this) { (pos, _) ->
                updateLyricHighlight(lyrics, pos)
            }
        }

        viewModel.loopMode.observe(this) { mode ->
            binding.btnLoopMode.setImageResource(
                when (mode) {
                    LoopMode.ORDER -> R.drawable.ic_repeat
                    LoopMode.LOOP -> R.drawable.ic_repeat_one
                    LoopMode.RANDOM -> R.drawable.ic_shuffle
                }
            )
        }

        viewModel.isFavorited.observe(this) { fav ->
            binding.btnFavorite.setImageResource(
                if (fav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
        }
    }

    private fun displaySong(song: Song) {
        binding.textTitle.text = song.name
        binding.textArtist.text = song.artistNames

        Glide.with(this).asBitmap().load(song.album.picUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.imageCd.setImageBitmap(resource)
                    binding.imageBg.setImageBitmap(resource)
                    applyPalette(resource)
                }
            })

        startCdRotation()
    }

    private fun applyPalette(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            val dominant = palette?.dominantSwatch?.rgb ?: 0xFF333333.toInt()
            binding.overlayBg.setBackgroundColor(dominant)
        }
    }

    private fun startCdRotation() {
        cdAnimator?.cancel()
        cdAnimator = ObjectAnimator.ofFloat(binding.imageCd, "rotation", 0f, 360f).apply {
            duration = 20_000
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.btnNext.setOnClickListener { viewModel.playNext() }
        binding.btnPrev.setOnClickListener { viewModel.playPrevious() }
        binding.btnLoopMode.setOnClickListener { viewModel.cycleLoopMode() }
        binding.btnFavorite.setOnClickListener { viewModel.toggleFavorite() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.textCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                seekBar?.progress?.toLong()?.let { viewModel.seekTo(it) }
            }
        })

        // Toggle lyrics view on CD click
        binding.imageCd.setOnClickListener {
            val lyricsVisible = binding.recyclerLyrics.visibility == View.VISIBLE
            binding.recyclerLyrics.visibility = if (lyricsVisible) View.GONE else View.VISIBLE
            binding.imageCd.visibility = if (lyricsVisible) View.VISIBLE else View.GONE
        }

        binding.recyclerLyrics.setOnClickListener {
            binding.recyclerLyrics.visibility = View.GONE
            binding.imageCd.visibility = View.VISIBLE
        }
    }

    private fun updateLyricHighlight(lyrics: List<LyricLine>, positionMs: Long) {
        val currentIndex = lyrics.indexOfLast { it.timeMs <= positionMs }
        if (currentIndex >= 0) {
            val layoutManager = binding.recyclerLyrics.layoutManager as? LinearLayoutManager
            val firstVisible = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
            val lastVisible = layoutManager?.findLastCompletelyVisibleItemPosition() ?: 0
            if (currentIndex !in firstVisible..lastVisible) {
                binding.recyclerLyrics.smoothScrollToPosition(currentIndex)
            }
            val adapter = binding.recyclerLyrics.adapter as? LyricAdapter
            adapter?.setHighlightIndex(currentIndex)
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%02d:%02d".format(min, sec)
    }

    override fun onDestroy() {
        super.onDestroy()
        cdAnimator?.cancel()
    }
}
