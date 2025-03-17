package com.boki.codev.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.security.crypto.password.PasswordEncoder

@Converter
class PasswordConverter(
    private val passwordEncoder: PasswordEncoder
) : AttributeConverter<String, String> {
    override fun convertToDatabaseColumn(attribute: String): String {
        // return BCryptUtil.hashPassword(attribute)
        return passwordEncoder.encode(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): String {
        return dbData
    }
}