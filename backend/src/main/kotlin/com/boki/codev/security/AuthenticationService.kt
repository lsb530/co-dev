package com.boki.codev.security

import com.boki.codev.redis.RefreshToken
import com.boki.codev.redis.RefreshTokenRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtTokenService: JwtTokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun getAuthentication(authenticationRequest: AuthenticationRequest): Authentication {
        val authenticate = authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.email,
                authenticationRequest.password
            )
        )
        return authenticate
    }

    fun getUserDetails(authenticationRequest: AuthenticationRequest): UserDetails {
        return userDetailsService.loadUserByUsername(authenticationRequest.email)
    }

    fun authentication(authenticationRequest: AuthenticationRequest): AuthenticationResponse {
        val authentication = getAuthentication(authenticationRequest)

        val accessToken = createAccessToken(authentication)
        val refreshToken = createRefreshToken(authentication)

        val email = authenticationRequest.email
        val foundRefreshToken = refreshTokenRepository.findByEmail(email)
        if (foundRefreshToken != null) {
            refreshTokenRepository.delete(foundRefreshToken)
        }

        refreshTokenRepository.save(RefreshToken(email, refreshToken))

        return AuthenticationResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun createAccessToken(authentication: Authentication): String {
        return jwtTokenService.generateAccessToken(authentication)
    }

    private fun createRefreshToken(authentication: Authentication): String {
        return jwtTokenService.generateRefreshToken(authentication)
    }
}