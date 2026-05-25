package ru.misis.booking.repository.jpa

import org.springframework.stereotype.Repository
import ru.misis.booking.domain.model.User
import ru.misis.booking.repository.UserRepository

@Repository
class UserRepositoryAdapter(private val jpa: JpaUserRepository) : UserRepository {
    override fun findById(id: Long): User? = jpa.findById(id).orElse(null)
    override fun existsByEmail(email: String): Boolean = jpa.existsByEmail(email)
    override fun existsByPhone(phone: String): Boolean = jpa.existsByPhone(phone)
    override fun save(user: User): User = jpa.save(user)
}
