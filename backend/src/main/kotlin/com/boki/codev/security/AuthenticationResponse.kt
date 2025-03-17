package com.boki.codev.security

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String,
)
