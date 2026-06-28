package com.music.player.data.remote

import com.google.gson.annotations.SerializedName
import com.music.player.data.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("banner")
    suspend fun getBanners(): List<Banner>

    @GET("personalized")
    suspend fun getPersonalized(@Query("limit") limit: Int = 6): PlaylistResult

    @GET("playlist/detail")
    suspend fun getPlaylistDetail(@Query("id") id: Long): PlaylistDetailResult

    @GET("song/url")
    suspend fun getSongUrl(@Query("id") id: Long): SongUrlResult

    @GET("song/detail")
    suspend fun getSongDetail(@Query("ids") ids: Long): SongDetailResult

    @GET("lyric")
    suspend fun getLyric(@Query("id") id: Long): LyricResult

    @GET("search")
    suspend fun search(@Query("keywords") keywords: String): SearchResult
}

data class SongDetailResult(
    @SerializedName("songs") val songs: List<Song>
) {
    fun getFirst(): Song? = songs.firstOrNull()
}
