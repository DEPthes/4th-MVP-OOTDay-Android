package com.bottari.ootday.domain.model

import com.bottari.ootday.data.service.MoodDto
import com.bottari.ootday.data.service.PlaceDto

data class StylingRequest(
    val imageList: List<ClothingItemDto>,
    val mood: List<MoodDto>,
    val place: PlaceDto
)