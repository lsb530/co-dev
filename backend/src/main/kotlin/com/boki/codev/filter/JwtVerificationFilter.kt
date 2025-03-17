package com.boki.codev.filter

import com.boki.codev.security.JwtTokenService
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.filter.OrderedFilter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtVerificationFilter(
    private val jwtTokenService: JwtTokenService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter(), OrderedFilter {

    override fun getOrder() = 1

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader: String? = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            val token: String = authorizationHeader.substringAfter("Bearer ")
            try {
                jwtTokenService.verifyToken(token)
            } catch (e: JwtException) {
                sendJwtErrorResponse(response, e)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun sendJwtErrorResponse(
        response: HttpServletResponse,
        e: JwtException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.writer.write(objectMapper.writeValueAsString(JwtError(e)))
    }

    data class JwtError(val code: String, val message: String?) {
        constructor(e: JwtException): this("JWT Error", e.message)
    }

}