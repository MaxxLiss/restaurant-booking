package ru.misis.booking.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import ru.misis.booking.domain.model.User

interface JpaUserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByPhone(phone: String): Boolean
}
