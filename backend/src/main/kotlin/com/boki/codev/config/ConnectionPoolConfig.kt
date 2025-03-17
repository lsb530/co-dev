package com.boki.codev.config

import com.boki.codev.prop.DatabaseProperties
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy

private val logger = KotlinLogging.logger {}

@EnableConfigurationProperties(DatabaseProperties::class)
@Configuration
class ConnectionPoolConfig(
    private val databaseProperties: DatabaseProperties,
) {
    @Bean
    fun lazyConnectionProxyDataSource(): LazyConnectionDataSourceProxy {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = databaseProperties.url
            username = databaseProperties.username
            password = databaseProperties.password
            maximumPoolSize = 20 // 10개 늘림
        }
        logger.info { "## Change: HikariCP -> LazyConnectionDataSourceProxy" }

        return LazyConnectionDataSourceProxy(HikariDataSource(hikariConfig))
    }
}