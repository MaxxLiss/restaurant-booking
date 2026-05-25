package ru.misis.booking.repository.jpa

import org.springframework.stereotype.Repository
import ru.misis.booking.domain.model.Booking
import ru.misis.booking.repository.BookingRepository

@Repository
class BookingRepositoryAdapter(private val jpa: JpaBookingRepository) : BookingRepository {
    override fun findById(id: Long): Booking? = jpa.findById(id).orElse(null)
    override fun findAllByUserId(userId: Long): List<Booking> = jpa.findAllByUserUserId(userId)
    override fun save(booking: Booking): Booking = jpa.save(booking)
}
