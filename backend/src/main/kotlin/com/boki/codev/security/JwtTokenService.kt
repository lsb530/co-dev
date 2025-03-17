package com.boki.codev.security

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.*
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}

@Service
class JwtTokenService(
    @Value("\${jwt.secret-key}")
    val secretKey: String = "",
    @Value("\${jwt.access-token-expiration}")
    val accessTokenExpiration: Long = 0,
    @Value("\${jwt.refresh-token-expiration}")
    val refreshTokenExpiration: Long = 0,
) {
    private val signingKey: SecretKeySpec
        get() {
            val keyBytes: ByteArray = Base64.getDecoder().decode(secretKey)
            return SecretKeySpec(keyBytes, 0, keyBytes.size, "HmacSHA256")
        }

    fun generateAccessToken(authentication: Authentication): String {
        val username = (authentication.principal as User).username
        val roles = authentication.authorities.map { SimpleGrantedAuthority(it?.authority) }.toList()
        return generateToken(username, accessTokenExpiration, mapOf("roles" to roles))
    }

    fun generateRefreshToken(authentication: Authentication): String {
        val username = (authentication.principal as User).username
        val roles = authentication.authorities.map { SimpleGrantedAuthority(it?.authority) }.toList()
        return generateToken(username, refreshTokenExpiration, mapOf("roles" to roles))
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .payload

    fun verifyToken(token: String) {
        try {
            extractAllClaims(token)
        } catch (e: JwtException) {
            var errMessage = ""
            when (e) {
                is SignatureException -> errMessage = "서명이 잘못된 JWT 토큰입니다."
                is MalformedJwtException -> errMessage = "잘못된 형식의 JWT 토큰입니다."
                is ExpiredJwtException -> errMessage = "만료된 JWT 토큰입니다."
                is UnsupportedJwtException -> errMessage = "지원하지 않는 JWT 토큰입니다."
            }
            logger.error { "JWT token verification failed: $errMessage" }
            throw JwtException(errMessage)
        }
    }

    private fun generateToken(subject: String, expiration: Long, additionalClaims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .claims(additionalClaims)
            .subject(subject)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(Date().time + expiration * 1_000))
            .signWith(signingKey)
            .compact()
    }
}