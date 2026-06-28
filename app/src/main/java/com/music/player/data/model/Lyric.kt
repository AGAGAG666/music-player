package com.music.player.data.model

import com.google.gson.annotations.SerializedName

data class LyricResult(
    @SerializedName("lrc") val lrc: LrcContent?
)

data class LrcContent(
    @SerializedName("lyric") val lyric: String?
)
