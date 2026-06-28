package com.music.player.data.model

import com.google.gson.annotations.SerializedName

data class SearchResult(
    @SerializedName("result") val result: SongResult?
)

data class SongResult(
    @SerializedName("songs") val songs: List<Song>?
)
