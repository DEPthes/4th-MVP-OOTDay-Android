package com.bottari.ootday.domain.repository.dataClass

data class AuthCheckRequest(
    val phoneNumber: String,
    val authCode: String,
)
