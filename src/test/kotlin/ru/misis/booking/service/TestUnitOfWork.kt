package ru.misis.booking.service

import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository
import ru.misis.booking.uow.IUnitOfWork

class TestUnitOfWork(
    override val restaurants: RestaurantRepository,
    override val users: UserRepository,
    override val bookings: BookingRepository
) : IUnitOfWork {
    override fun <T> execute(block: IUnitOfWork.() -> T): T = block()
    override fun <T> executeReadOnly(block: IUnitOfWork.() -> T): T = block()
}
