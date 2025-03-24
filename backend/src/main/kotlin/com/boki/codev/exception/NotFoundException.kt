package com.boki.codev.exception

data class NotFoundException(
    override val message: String,
    override val cause: Throwable?,
): NoSuchElementException() {
    constructor(message: String): this(message = message, cause = null)
}
