package com.bottari.ootday.domain.model

import com.google.gson.annotations.SerializedName

data class ClosetItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("mood")
    val mood: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("imageUrl")
    val imageUrl: String

)
