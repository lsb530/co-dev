package com.boki.codev.security

import com.boki.codev.redis.RefreshToken
import com.boki.codev.redis.RefreshTokenRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun authentication(authenticationRequest: AuthenticationRequest): AuthenticationResponse {
        logger.info { "<< ${authenticationRequest.email} login requested" }

        val authentication = getAuthentication(authenticationRequest)

        val accessToken = createAccessToken(authentication)
        val refreshToken = createRefreshToken(authentication)

        val email = authenticationRequest.email
        val foundRefreshToken = refreshTokenRepository.findByEmail(email)
        if (foundRefreshToken != null) {
            refreshTokenRepository.delete(foundRefreshToken)
        }

        logger.info { ">> ${authenticationRequest.email} login successfully" }

        refreshTokenRepository.save(RefreshToken(email, refreshToken))

        return AuthenticationResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun getAuthentication(authenticationRequest: AuthenticationRequest): Authentication {
        val authenticate = authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.email,
                authenticationRequest.password
            )
        )
        return authenticate
    }

    private fun createAccessToken(authentication: Authentication): String {
        return jwtTokenService.generateAccessToken(authentication)
    }

    private fun createRefreshToken(authentication: Authentication): String {
        return jwtTokenService.generateRefreshToken(authentication)
    }
}