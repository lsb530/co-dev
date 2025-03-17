package com.boki.codev.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import java.util.concurrent.TimeUnit

@RedisHash(value = "refresh_token", timeToLive = 1296000 * 1000)
class RefreshToken(
    @Id
    val email: String,

    @Indexed
    var token: String,

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    var expiration: Long? = 1296000 * 1000,
) {
    fun update(token: String, ttl: Long) {
        this.token = token
        this.expiration = ttl
    }
}