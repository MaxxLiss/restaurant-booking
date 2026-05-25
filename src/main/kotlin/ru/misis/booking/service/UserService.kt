package ru.misis.booking.service

import org.springframework.stereotype.Service
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.User
import ru.misis.booking.dto.*
import ru.misis.booking.uow.IUnitOfWork

@Service
class UserService(
    private val uow: IUnitOfWork
) {

    fun register(request: RegisterUserRequest): RegisterResponse =
        uow.execute {
            if (users.existsByEmail(request.email))
                throw BusinessRuleViolationException("Email уже используется")
            if (users.existsByPhone(request.phone))
                throw BusinessRuleViolationException("Телефон уже используется")
            val user = User(request.email, request.phone, hash(request.password))
            RegisterResponse.from(users.save(user))
        }

    fun getUser(id: Long): UserProfileResponse =
        uow.executeReadOnly {
            UserProfileResponse.from(findOrThrow(id))
        }

    fun updateProfile(id: Long, request: UpdateProfileRequest): UpdateProfileResponse =
        uow.execute {
            val user = findOrThrow(id)
            user.updateProfile(request.email, request.phone)
            UpdateProfileResponse.from(users.save(user))
        }

    fun getUserBookings(id: Long): List<BookingSummaryResponse> =
        uow.executeReadOnly {
            bookings.findAllByUserId(id).map { BookingSummaryResponse.from(it) }
        }

    private fun IUnitOfWork.findOrThrow(id: Long): User =
        users.findById(id) ?: throw EntityNotFoundException("Пользователь $id не найден")

    private fun hash(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
