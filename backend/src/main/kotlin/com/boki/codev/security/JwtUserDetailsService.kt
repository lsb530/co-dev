package com.boki.codev.security

import com.boki.codev.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class JwtUserDetailsService(
    private val userRepository: UserRepository,
): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = (userRepository.findUserByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username"))

        return User.builder()
            .username(user.email)
            .password(user.password)
            .roles(user.role.name)
            .build()
    }
}