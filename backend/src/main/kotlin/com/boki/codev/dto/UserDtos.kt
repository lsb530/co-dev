package com.boki.codev.dto

import com.boki.codev.entity.user.User

data class UserResponse(
    val id: Long,
    val email: String,
    val username: String,
    val role: String,
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                username = user.username,
                role = user.role.name
            )
        }
    }
}