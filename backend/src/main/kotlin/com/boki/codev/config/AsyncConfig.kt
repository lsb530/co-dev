package com.boki.codev.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 25
        executor.setThreadNamePrefix("MyAsyncExecutor--")
        executor.initialize()
        return executor
    }

}