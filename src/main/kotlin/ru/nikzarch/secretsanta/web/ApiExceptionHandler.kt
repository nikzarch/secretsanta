package ru.nikzarch.secretsanta.web

import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.nikzarch.secretsanta.service.exception.NotFoundException


private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun notFound(ex: NotFoundException) = ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiErrorResponseDto(ex.message ?: "not found"))

    @ExceptionHandler(BadCredentialsException::class)
    fun badCredentials(ex: BadCredentialsException) = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiErrorResponseDto("invalid credentials"))

    @ExceptionHandler(AccessDeniedException::class)
    fun forbidden(ex: AccessDeniedException) = ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiErrorResponseDto("access denied"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException) = ResponseEntity.badRequest()
        .body(ApiErrorResponseDto(ex.message ?: "invalid request"))

    @ExceptionHandler(IllegalStateException::class, DataIntegrityViolationException::class)
    fun conflict(ex: RuntimeException): ResponseEntity<ApiErrorResponseDto> {
        logger.warn { "Operation conflict: ${ex.javaClass.simpleName}" }
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponseDto("operation conflicts with current state"))
    }

    @ExceptionHandler(RuntimeException::class)
    fun exception(ex: RuntimeException) = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiErrorResponseDto("Something went wrong"))
}

data class ApiErrorResponseDto(val error: String)

