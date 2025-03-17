package com.boki.codev.redis

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository: CrudRepository<RefreshToken, String> {
    fun findByToken(token: String): RefreshToken?
    fun findByEmail(email: String): RefreshToken?
}