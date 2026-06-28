package com.music.player.data.model

import com.google.gson.annotations.SerializedName

data class PlaylistResult(
    @SerializedName("result") val result: List<Playlist>
)

data class Playlist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("coverImgUrl") val coverImgUrl: String,
    @SerializedName("playCount") val playCount: Long,
    @SerializedName("description") val description: String?,
    @SerializedName("creator") val creator: Creator?,
    @SerializedName("tracks") val tracks: List<Song>?
)

data class Creator(
    @SerializedName("nickname") val nickname: String
)

data class PlaylistDetailResult(
    @SerializedName("playlist") val playlist: Playlist
)
