package com.boki.codev.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestValueException
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
        logger.warn { "Request Field Exception" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FieldErrorResponse(errors = fieldErrorsBodies))
    }

    @ExceptionHandler(value = [NotFoundException::class])
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn { "Not Found Exception" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(code = "Not Found", message = e.message))
    }

    @ExceptionHandler(value = [IllegalStateException::class])
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        logger.warn { "Bad Request Exception" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(code = "Illegal State", message = e.message))
    }

    @ExceptionHandler(value = [IllegalArgumentException::class])
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn { "Bad Request Exception" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(code = "Illegal Argument", message = e.message))
    }

    @ExceptionHandler(value = [MissingRequestValueException::class])
    fun handleMissingRequestValueException(e: MissingRequestValueException): ResponseEntity<ErrorResponse> {
        logger.warn { "Missing Request Value Exception" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "Missing Request Value", message = e.message))
    }

}