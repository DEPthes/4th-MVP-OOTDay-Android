package com.bottari.ootday.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecommendedItem(
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("purchaseUrl")
    val purchaseUrl: String
) : Parcelable
