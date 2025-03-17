package com.boki.codev.service

import com.boki.codev.exception.NoSuchCodeException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Service
class AuthCodeService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val CODE_LENGTH = 8
        private const val TTL_MINUTES = 2L
        private val CHARS = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    }

    fun generateCode(): String {
        val randomCode = (1..CODE_LENGTH)
            .map { CHARS[Random.nextInt(0, CHARS.size)] }
            .joinToString("")

        redisTemplate.opsForValue().set(randomCode, "false", Duration.ofMinutes(TTL_MINUTES))
        return randomCode
    }

    fun verifyCode(code: String): Boolean {
        val value = redisTemplate.opsForValue().get(code) ?: throw NoSuchCodeException()
        if (value == "true") {
            throw NoSuchCodeException("Code already verified")
        }
        redisTemplate.opsForValue().set(code, "true")
        return true
    }
}
