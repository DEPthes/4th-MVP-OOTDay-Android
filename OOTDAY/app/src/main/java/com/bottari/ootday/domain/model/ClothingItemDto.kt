package com.bottari.ootday.domain.model

data class ClothingItemDto(
    val uuid: String,
    val name: String,
    val category: String,
    val mood: String,
    val description: String,
    val imageUrl: String
)