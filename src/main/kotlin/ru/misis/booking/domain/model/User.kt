package ru.misis.booking.domain.model

import ru.misis.booking.domain.enums.Role
import ru.misis.booking.domain.exceptions.InvalidArgumentException
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = false, unique = true)
    var phone: String,
    @Column(name = "password_hash", nullable = false)
    private val passwordHash: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.CLIENT,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val userId: Long = 0
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _bookings: MutableList<Booking> = mutableListOf()

    init {
        require(EMAIL_REGEX.matches(email)) { "Некорректный email" }
        require(PHONE_REGEX.matches(phone)) { "Некорректный номер телефона (ожидается формат +7XXXXXXXXXX)" }
        require(passwordHash.isNotBlank()) { "passwordHash не может быть пустым" }
    }

    fun login(passwordHashAttempt: String): Boolean = passwordHash == passwordHashAttempt

    fun updateProfile(newEmail: String? = null, newPhone: String? = null) {
        newEmail?.let {
            if (!EMAIL_REGEX.matches(it)) throw InvalidArgumentException("Некорректный email")
            email = it
        }
        newPhone?.let {
            if (!PHONE_REGEX.matches(it)) throw InvalidArgumentException("Некорректный номер телефона")
            phone = it
        }
    }

    fun getBookingHistory(): List<Booking> = _bookings.toList()

    internal fun addBooking(booking: Booking) { _bookings.add(booking) }

    fun isAdmin(): Boolean = role == Role.ADMIN

    companion object {
        private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
        // E.164: + и от 7 до 15 цифр
        private val PHONE_REGEX = Regex("^\\+\\d{7,15}$")
    }
}
