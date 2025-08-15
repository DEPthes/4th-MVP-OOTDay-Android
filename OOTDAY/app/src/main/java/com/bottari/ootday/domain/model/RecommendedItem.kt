package com.bottari.ootday.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecommendedItem(
    val imageUrl: String,
    val productUrl: String,
) : Parcelable
