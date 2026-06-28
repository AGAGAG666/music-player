package com.music.player.ui

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.music.player.data.repository.MusicRepository
import com.music.player.databinding.ActivitySearchBinding
import com.music.player.ui.adapter.SearchResultAdapter
import com.music.player.viewmodel.MusicViewModel
import kotlinx.coroutines.*

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: MusicViewModel
    private val repository = MusicRepository()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.editSearch.text.toString())
                true
            } else false
        }

        // Debounced search
        binding.editSearch.addTextChangedListener(object : android.text.TextWatcher {
            private var searchRunnable: Runnable? = null
            private val handler = android.os.Handler(android.os.Looper.getMainLooper())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    val query = s?.toString()?.trim() ?: ""
                    if (query.isNotEmpty()) performSearch(query)
                }
                handler.postDelayed(searchRunnable!!, 500)
            }
        })
    }

    private fun performSearch(keywords: String) {
        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.Main).launch {
            val result = repository.searchSongs(keywords)
            result.fold(
                onSuccess = { songs ->
                    if (songs.isEmpty()) {
                        Toast.makeText(this@SearchActivity, "未找到相关歌曲", Toast.LENGTH_SHORT).show()
                    }
                    val adapter = SearchResultAdapter(songs) { song ->
                        viewModel.playSong(song)
                    }
                    binding.recyclerResults.layoutManager = LinearLayoutManager(this@SearchActivity)
                    binding.recyclerResults.adapter = adapter
                },
                onFailure = {
                    Toast.makeText(this@SearchActivity, "搜索失败", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
