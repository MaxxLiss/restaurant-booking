package ru.misis.booking.domain.model

import ru.misis.booking.domain.enums.Channel
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
data class Notification(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false, length = 1000)
    val message: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val channel: Channel,
    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,
    @Column(nullable = false)
    var sent: Boolean = false,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notify_id")
    val notifyId: Long = 0
) {
    init {
        require(message.isNotBlank()) { "Сообщение не может быть пустым" }
    }

    fun send(): Boolean {
        if (sent) return true
        sent = true
        sentAt = LocalDateTime.now()
        return true
    }
}
