package com.boki.codev.security

data class AuthenticationRequest(
    val email: String,
    val password: String
)