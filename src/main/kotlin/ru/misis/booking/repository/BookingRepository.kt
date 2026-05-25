package ru.misis.booking.repository

import ru.misis.booking.domain.model.Booking

interface BookingRepository {
    fun findById(id: Long): Booking?
    fun findAllByUserId(userId: Long): List<Booking>
    fun save(booking: Booking): Booking
}
