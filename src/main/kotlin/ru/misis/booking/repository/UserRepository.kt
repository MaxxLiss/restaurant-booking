package ru.misis.booking.repository

import ru.misis.booking.domain.model.User

interface UserRepository {
    fun findById(id: Long): User?
    fun existsByEmail(email: String): Boolean
    fun existsByPhone(phone: String): Boolean
    fun save(user: User): User
}
