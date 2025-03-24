package com.boki.codev.dto

import org.springframework.security.core.userdetails.UserDetails

data class MyInfo(
    val username: String,
    val authorities: List<String>
) {
    companion object {
        fun fromUserDetails(userDetails: UserDetails): MyInfo {
            return MyInfo(
                username = userDetails.username,
                authorities = userDetails.authorities.map { it.authority }
            )
        }
    }

    fun toLoggerString(): String {
        return "username: $username, authorities: ${authorities.joinToString(", ")}"
    }
}
