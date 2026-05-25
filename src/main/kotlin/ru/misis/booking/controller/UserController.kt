package ru.misis.booking.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.misis.booking.dto.*
import ru.misis.booking.service.UserService

@Tag(name = "Users", description = "Регистрация и управление профилем пользователя")
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @Operation(summary = "Зарегистрировать пользователя")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterUserRequest): RegisterResponse =
        userService.register(request)

    @Operation(summary = "Получить профиль пользователя")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserProfileResponse = userService.getUser(id)

    @Operation(summary = "Обновить email или телефон")
    @PatchMapping("/{id}")
    fun updateProfile(@PathVariable id: Long, @RequestBody request: UpdateProfileRequest): UpdateProfileResponse =
        userService.updateProfile(id, request)

    @Operation(summary = "История бронирований пользователя")
    @GetMapping("/{id}/bookings")
    fun getUserBookings(@PathVariable id: Long): List<BookingSummaryResponse> =
        userService.getUserBookings(id)
}
