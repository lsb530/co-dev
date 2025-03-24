package com.boki.codev.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

@Component
// @Order(Ordered.HIGHEST_PRECEDENCE) // Spring Security 적용으로 인증필터 이후로 미뤄야되기 때문에 순서 변경
class MDCLoggingFilter: Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val requestId = (request as HttpServletRequest).getHeader("X-RequestID")
            ?: UUID.randomUUID().toString().replace("-", "")
        MDC.put("request_id", requestId)

        chain.doFilter(request, response)

        MDC.clear()
    }

}