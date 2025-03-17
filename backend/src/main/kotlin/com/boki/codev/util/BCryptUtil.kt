package com.boki.codev.util

import org.mindrot.jbcrypt.BCrypt

/**
 * Spring Security 붙이기 전 개발용으로 해시함수 사용할 유틸
 */
object BCryptUtil {
    fun hashPassword(plainPassword: String): String {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt())
    }

    fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }
}