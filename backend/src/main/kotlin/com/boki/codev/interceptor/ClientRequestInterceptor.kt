package com.boki.codev.interceptor

import com.boki.codev.entity.httpinterface.HttpInterface
import com.boki.codev.repository.HttpInterfaceRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception

private val logger = KotlinLogging.logger {}

@Component
class ClientRequestInterceptor(
    private val httpInterfaceRepository: HttpInterfaceRepository,
) : HandlerInterceptor {
    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        logger.info { "Handled ${request.method} ${request.requestURI}" }
        httpInterfaceRepository.save(HttpInterface(request))
    }
}