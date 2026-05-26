package ru.misis.booking.notification

import ru.misis.booking.domain.enums.Channel
import ru.misis.booking.domain.model.User
import java.time.LocalDateTime

data class Notification(
    val user: User,
    val message: String,
    val channel: Channel,
    var sentAt: LocalDateTime? = null,
    var sent: Boolean = false
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