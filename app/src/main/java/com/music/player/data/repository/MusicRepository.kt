package com.music.player.data.repository

import com.music.player.data.model.*
import com.music.player.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository {

    private val api = RetrofitClient.apiService

    suspend fun fetchBanners(): Result<List<Banner>> = runCatching {
        withContext(Dispatchers.IO) { api.getBanners() }
    }

    suspend fun fetchPersonalized(limit: Int = 6): Result<List<Playlist>> = runCatching {
        withContext(Dispatchers.IO) { api.getPersonalized(limit).result }
    }

    suspend fun fetchPlaylistDetail(id: Long): Result<Playlist> = runCatching {
        withContext(Dispatchers.IO) { api.getPlaylistDetail(id).playlist }
    }

    suspend fun fetchSongUrl(id: Long): Result<String?> = runCatching {
        withContext(Dispatchers.IO) { api.getSongUrl(id).data.firstOrNull()?.url }
    }

    suspend fun fetchSongDetail(id: Long): Result<Song?> = runCatching {
        withContext(Dispatchers.IO) { api.getSongDetail(id).getFirst() }
    }

    suspend fun fetchLyric(id: Long): Result<String?> = runCatching {
        withContext(Dispatchers.IO) { api.getLyric(id).lrc?.lyric }
    }

    suspend fun searchSongs(keywords: String): Result<List<Song>> = runCatching {
        withContext(Dispatchers.IO) { api.search(keywords).result?.songs ?: emptyList() }
    }
}
