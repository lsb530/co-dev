package com.boki.codev.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt

class BCryptUtilTest {
    @Test
    fun encrypt() {
        val password = "hello"
        val hashPassword = BCryptUtil.hashPassword(password)
        println(hashPassword)
        println(hashPassword.length)

        val isSame = BCrypt.checkpw(password, "\$2a\$10\$gpSoCHprzTKHRrmTm7PS1e49mHjC3qkSN1COcGtB.QEgvjRl2dX2C")
        println(isSame)
    }
}