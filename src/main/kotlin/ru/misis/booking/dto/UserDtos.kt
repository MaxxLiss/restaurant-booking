package ru.misis.booking.dto

import jakarta.validation.constraints.NotBlank
import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.Role
import ru.misis.booking.domain.model.Booking
import ru.misis.booking.domain.model.User
import java.math.BigDecimal
import java.time.LocalDateTime

data class RegisterUserRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val phone: String,
    @field:NotBlank val password: String
)

data class UpdateProfileRequest(
    val email: String? = null,
    val phone: String? = null
)

data class RegisterResponse(val userId: Long, val email: String, val phone: String) {
    companion object { fun from(u: User) = RegisterResponse(u.userId, u.email, u.phone) }
}

data class UserProfileResponse(val userId: Long, val email: String, val phone: String, val role: Role) {
    companion object { fun from(u: User) = UserProfileResponse(u.userId, u.email, u.phone, u.role) }
}

data class UpdateProfileResponse(val userId: Long, val email: String, val phone: String) {
    companion object { fun from(u: User) = UpdateProfileResponse(u.userId, u.email, u.phone) }
}

data class BookingSummaryResponse(
    val bookingId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val guests: Int,
    val status: BookingStatus,
    val total: BigDecimal
) {
    companion object {
        fun from(b: Booking) = BookingSummaryResponse(b.bookingId, b.startAt, b.endAt, b.guests, b.status, b.calculateTotal())
    }
}
