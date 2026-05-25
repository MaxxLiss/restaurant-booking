package ru.misis.booking.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.User
import ru.misis.booking.dto.*
import ru.misis.booking.repository.UserRepository

@Service
@Transactional
class UserService(private val userRepository: UserRepository) {

    fun register(request: RegisterUserRequest): RegisterResponse {
        if (userRepository.existsByEmail(request.email))
            throw BusinessRuleViolationException("Email уже используется")
        if (userRepository.existsByPhone(request.phone))
            throw BusinessRuleViolationException("Телефон уже используется")
        val user = User(request.email, request.phone, hash(request.password))
        return RegisterResponse.from(userRepository.save(user))
    }

    @Transactional(readOnly = true)
    fun getUser(id: Long): UserProfileResponse = UserProfileResponse.from(findOrThrow(id))

    fun updateProfile(id: Long, request: UpdateProfileRequest): UpdateProfileResponse {
        val user = findOrThrow(id)
        user.updateProfile(request.email, request.phone)
        return UpdateProfileResponse.from(userRepository.save(user))
    }

    @Transactional(readOnly = true)
    fun getUserBookings(id: Long): List<BookingSummaryResponse> {
        val user = findOrThrow(id)
        return user.getBookingHistory().map { BookingSummaryResponse.from(it) }
    }

    private fun findOrThrow(id: Long): User =
        userRepository.findById(id).orElseThrow { EntityNotFoundException("Пользователь $id не найден") }

    private fun hash(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
