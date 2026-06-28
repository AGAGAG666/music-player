package com.music.player.data.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName(value = "ar", alternate = ["artists"]) val artists: List<Artist>,
    @SerializedName(value = "al", alternate = ["album"]) val album: Album,
    @SerializedName("dt") val duration: Long
) {
    val artistNames: String
        get() = artists.joinToString("/") { it.name }

    val durationFormatted: String
        get() {
            val totalSec = duration / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return "%02d:%02d".format(min, sec)
        }
}

data class Artist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)

data class Album(
    @SerializedName("id") val id: Long,
    @SerializedName("picUrl") val picUrl: String
)
