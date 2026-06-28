package com.music.player.data.model

import com.google.gson.annotations.SerializedName

data class Banner(
    @SerializedName("id") val id: Long,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("typeTitle") val typeTitle: String?
)
