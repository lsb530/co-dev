package com.boki.codev.repository

import com.boki.codev.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findUserByEmail(email: String): User?
}