package com.boki.codev.converter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder

class PasswordConverterTest : BehaviorSpec({

    val passwordEncoder = mockk<PasswordEncoder>()
    val passwordConverter = PasswordConverter(passwordEncoder)

    given("a password converter") {

        `when`("converting a password to database column") {
            val rawPassword = "mySecretPassword"
            val encodedPassword = "encodedPassword123"

            every { passwordEncoder.encode(rawPassword) } returns encodedPassword

            then("it should encode the password") {
                val result = passwordConverter.convertToDatabaseColumn(rawPassword)

                result shouldBe encodedPassword
                verify(exactly = 1) { passwordEncoder.encode(rawPassword) }
            }
        }

        `when`("converting from database column to entity attribute") {
            val dbData = "storedEncodedPassword"

            then("it should return the data as is") {
                val result = passwordConverter.convertToEntityAttribute(dbData)

                result shouldBe dbData
            }
        }
    }
})