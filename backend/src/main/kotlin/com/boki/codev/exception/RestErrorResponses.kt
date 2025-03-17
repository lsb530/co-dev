package com.boki.codev.exception

data class ErrorResponse(val code: String, val message: String?)

data class FieldErrorResponse(
    val code: String,
    val errors: List<FieldErrorsBody>,
) {
    constructor(errors: List<FieldErrorsBody>): this(code = "Field Error", errors = errors)
}

data class FieldErrorsBody(
    val fieldName: String,
    val requestValue: Any?,
    val message: String?,
)