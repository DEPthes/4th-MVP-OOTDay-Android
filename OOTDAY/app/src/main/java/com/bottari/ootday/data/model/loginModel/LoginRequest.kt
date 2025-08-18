package com.bottari.ootday.data.model.loginModel

data class LoginRequest(
    val memberId: String,
    val password: String,
    val rememberMe: Boolean
)
