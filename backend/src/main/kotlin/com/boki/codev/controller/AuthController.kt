package com.boki.codev.controller

import com.boki.codev.dto.MyInfo
import com.boki.codev.security.AuthenticationRequest
import com.boki.codev.security.AuthenticationResponse
import com.boki.codev.security.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
) {
    @PostMapping
    fun login(@RequestBody request: AuthenticationRequest): AuthenticationResponse {
        println(request)
        return authenticationService.authentication(request)
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    fun me(): ResponseEntity<Any> {
        val authentication = SecurityContextHolder.getContext().authentication
        val response = MyInfo.fromUserDetails(authentication.principal as UserDetails)
        return ResponseEntity.ok(response)
    }
}