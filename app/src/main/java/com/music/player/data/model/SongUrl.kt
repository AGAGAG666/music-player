package com.music.player.data.model

import com.google.gson.annotations.SerializedName

data class SongUrlResult(
    @SerializedName("data") val data: List<SongUrl>
)

data class SongUrl(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String?,
    @SerializedName("br") val br: Int,
    @SerializedName("size") val size: Long
)
