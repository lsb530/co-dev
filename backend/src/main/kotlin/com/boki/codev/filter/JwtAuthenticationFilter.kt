package com.boki.codev.filter

import com.boki.codev.security.JwtTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.filter.OrderedFilter
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtTokenService: JwtTokenService,
) : OncePerRequestFilter(), OrderedFilter {

    override fun getOrder(): Int = 2

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader: String? = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            val token = authorizationHeader.substringAfter("Bearer ")
            val usernameFromToken = jwtTokenService.extractUsername(token)
            if (SecurityContextHolder.getContext().authentication == null) {
                val userDetails: UserDetails = userDetailsService.loadUserByUsername(usernameFromToken)
                if (usernameFromToken == userDetails.username) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}