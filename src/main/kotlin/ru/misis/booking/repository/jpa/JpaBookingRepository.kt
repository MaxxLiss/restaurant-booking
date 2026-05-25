package ru.misis.booking.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import ru.misis.booking.domain.model.Booking

interface JpaBookingRepository : JpaRepository<Booking, Long> {
    fun findAllByUserUserId(userId: Long): List<Booking>
}
