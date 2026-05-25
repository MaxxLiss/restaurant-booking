package ru.misis.booking.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.exceptions.InvalidArgumentException
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(status = 404, error = "Not Found", message = ex.message ?: ""))

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRule(ex: BusinessRuleViolationException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(status = 422, error = "Unprocessable Entity", message = ex.message ?: ""))

    @ExceptionHandler(InvalidArgumentException::class, IllegalArgumentException::class)
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(status = 400, error = "Bad Request", message = ex.message ?: ""))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(status = 400, error = "Validation Failed", message = message))
    }
}
