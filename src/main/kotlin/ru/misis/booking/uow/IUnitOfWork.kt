package ru.misis.booking.uow

import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository

interface IUnitOfWork {
    val restaurants: RestaurantRepository
    val users: UserRepository
    val bookings: BookingRepository

    /** Выполняет блок в рамках одной транзакции. При исключении — откат. */
    fun <T> execute(block: IUnitOfWork.() -> T): T

    /** Выполняет блок в read-only транзакции (без возможности записи). */
    fun <T> executeReadOnly(block: IUnitOfWork.() -> T): T
}
