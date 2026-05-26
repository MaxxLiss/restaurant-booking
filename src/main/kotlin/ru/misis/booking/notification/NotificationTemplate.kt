package ru.misis.booking.notification

import ru.misis.booking.domain.enums.Channel
import ru.misis.booking.domain.model.User

abstract class NotificationTemplate(val rawMessage: String) {
    abstract val channel: Channel

    abstract fun copy(): NotificationTemplate
    abstract fun createNotification(user: User): Notification
}

class EmailTemplate(
    rawMessage: String,
    val subjectPrefix: String = "Уведомление"
) : NotificationTemplate(rawMessage) {
    override val channel = Channel.EMAIL

    override fun createNotification(user: User) =
        Notification(user = user, message = "[$subjectPrefix] $rawMessage", channel = channel)

    override fun copy() = EmailTemplate(rawMessage, subjectPrefix)
}

class SmsTemplate(
    rawMessage: String,
    val maxLength: Int = 160
) : NotificationTemplate(rawMessage) {
    override val channel = Channel.SMS

    override fun createNotification(user: User) =
        Notification(user = user, message = rawMessage.take(maxLength), channel = channel)

    override fun copy() = SmsTemplate(rawMessage, maxLength)
}

class PushTemplate(
    rawMessage: String,
    val titleMaxLength: Int = 50
) : NotificationTemplate(rawMessage) {
    override val channel = Channel.PUSH

    override fun createNotification(user: User): Notification {
        val message = if (rawMessage.length > titleMaxLength)
            rawMessage.take(titleMaxLength).trimEnd() + "…"
        else
            rawMessage
        return Notification(user = user, message = message, channel = channel)
    }

    override fun copy() = PushTemplate(rawMessage, titleMaxLength)
}
