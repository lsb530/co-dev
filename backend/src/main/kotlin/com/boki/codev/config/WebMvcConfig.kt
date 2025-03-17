package com.boki.codev.config

import com.boki.codev.interceptor.ClientRequestInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val clientRequestInterceptor: ClientRequestInterceptor
): WebMvcConfigurer {
    companion object {
        private val EXCLUDE_PATH_PATTERN_LIST = listOf(
            "/assets/**", "/js/**", "/css/**", "/favicon.ico", "/admin/**", "h2**", "/error",
        )
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(clientRequestInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(EXCLUDE_PATH_PATTERN_LIST)
    }
}