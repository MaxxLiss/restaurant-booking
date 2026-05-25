package ru.misis.booking.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.misis.booking.domain.model.Booking

interface BookingRepository : JpaRepository<Booking, Long> {
    fun findAllByUserUserId(userId: Long): List<Booking>
}
