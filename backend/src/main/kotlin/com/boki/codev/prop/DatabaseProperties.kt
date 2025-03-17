package com.boki.codev.prop

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding

@ConfigurationPropertiesBinding
@ConfigurationProperties(prefix = "spring.datasource")
data class DatabaseProperties(
    val url: String,
    val username: String,
    val password: String,
)