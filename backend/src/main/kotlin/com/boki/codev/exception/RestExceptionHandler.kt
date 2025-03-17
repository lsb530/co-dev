package com.boki.codev.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<FieldErrorResponse> {
        val fieldErrorsBodies = e.bindingResult.fieldErrors.map { error ->
            FieldErrorsBody(fieldName = error.field, requestValue = error.rejectedValue, message = error.defaultMessage)
        }.toList()
        logger.warn { "Request Field Error" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FieldErrorResponse(errors = fieldErrorsBodies))
    }

}