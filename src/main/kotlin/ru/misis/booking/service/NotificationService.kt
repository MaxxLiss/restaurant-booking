package ru.misis.booking.service

import org.springframework.stereotype.Service
import ru.misis.booking.notification.Notification
import ru.misis.booking.notification.NotificationTemplate
import ru.misis.booking.uow.IUnitOfWork

@Service
class NotificationService(private val uow: IUnitOfWork) {
    fun notifyAll(userIds: List<Long>, template: NotificationTemplate): List<Notification> =
        uow.executeReadOnly {
            userIds.mapNotNull { id ->
                users.findById(id)?.let { user -> template.copy().createNotification(user) }
            }
        }
}
