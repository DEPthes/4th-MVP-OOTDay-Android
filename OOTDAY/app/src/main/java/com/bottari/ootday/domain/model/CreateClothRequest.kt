package com.bottari.ootday.domain.model

data class CreateClothRequest(
    val image: String, // 이미지 URL
    val category: String
)