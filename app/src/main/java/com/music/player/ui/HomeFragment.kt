package com.music.player.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.music.player.R
import com.music.player.data.model.Banner
import com.music.player.data.model.Playlist
import com.music.player.data.repository.MusicRepository
import com.music.player.databinding.FragmentHomeBinding
import com.music.player.ui.adapter.BannerAdapter
import com.music.player.ui.adapter.PlaylistAdapter
import com.music.player.viewmodel.MusicViewModel
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MusicViewModel
    private val repository = MusicRepository()
    private val handler = Handler(Looper.getMainLooper())
    private var bannerAutoScroll: Runnable? = null
    private var bannerPage = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_search -> {
                    startActivity(Intent(requireContext(), SearchActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadBanners()
        loadPlaylists()
    }

    private fun loadBanners() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = repository.fetchBanners()
            result.fold(
                onSuccess = { banners ->
                    setupBanner(banners)
                },
                onFailure = { }
            )
        }
    }

    private fun setupBanner(banners: List<Banner>) {
        if (banners.isEmpty()) return
        val adapter = BannerAdapter(banners)
        binding.viewPagerBanner.adapter = adapter

        // Create dot indicators
        binding.indicatorContainer.removeAllViews()
        banners.forEachIndexed { index, _ ->
            val dot = ImageView(requireContext()).apply {
                val size = resources.getDimensionPixelSize(R.dimen.dot_size)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = 4
                    marginEnd = 4
                }
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (index == 0) R.drawable.dot_selected else R.drawable.dot_unselected
                    )
                )
            }
            binding.indicatorContainer.addView(dot)
        }

        binding.viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bannerPage = position
                updateDots(banners.size, position)
            }
        })

        // Auto scroll
        bannerAutoScroll = object : Runnable {
            override fun run() {
                val next = (bannerPage + 1) % banners.size
                binding.viewPagerBanner.setCurrentItem(next, true)
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(bannerAutoScroll!!, 3000)
    }

    private fun updateDots(count: Int, selected: Int) {
        for (i in 0 until binding.indicatorContainer.childCount) {
            val dot = binding.indicatorContainer.getChildAt(i) as ImageView
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (i == selected) R.drawable.dot_selected else R.drawable.dot_unselected
                )
            )
        }
    }

    private fun loadPlaylists() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = repository.fetchPersonalized(6)
            result.fold(
                onSuccess = { playlists ->
                    setupPlaylistGrid(playlists)
                },
                onFailure = { }
            )
        }
    }

    private fun setupPlaylistGrid(playlists: List<Playlist>) {
        val adapter = PlaylistAdapter(playlists) { playlist ->
            val intent = Intent(requireContext(), PlaylistDetailActivity::class.java).apply {
                putExtra("playlist_id", playlist.id)
            }
            startActivity(intent)
        }
        binding.recyclerPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerPlaylists.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerAutoScroll?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}
