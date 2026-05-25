package ru.misis.booking.domain.model

import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.TableStatus
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "tables")
data class RestaurantTable(
    @Column(nullable = false)
    val capacity: Int,
    @Column(name = "pos_x", nullable = false)
    val posX: Float,
    @Column(name = "pos_y", nullable = false)
    val posY: Float,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TableStatus = TableStatus.FREE,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    var restaurant: Restaurant? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    val tableId: Long = 0
) {
    @OneToMany(mappedBy = "table", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _bookings: MutableList<Booking> = mutableListOf()

    init {
        require(capacity > 0) { "Вместимость столика должна быть положительной" }
    }

    internal fun assignTo(restaurant: Restaurant) { this.restaurant = restaurant }

    fun isAvailable(date: LocalDate, time: LocalTime): Boolean {
        if (status == TableStatus.BUSY) return false
        return _bookings.none { b -> b.status == BookingStatus.CONFIRMED && b.date == date && b.time == time }
    }

    internal fun registerBooking(booking: Booking) {
        if (booking.guests > capacity)
            throw BusinessRuleViolationException(
                "Количество гостей (${booking.guests}) превышает вместимость столика ($capacity)"
            )
        if (!isAvailable(booking.date, booking.time))
            throw BusinessRuleViolationException(
                "Столик $tableId уже занят на ${booking.date} ${booking.time}"
            )
        _bookings.add(booking)
    }

    internal fun releaseBooking(booking: Booking) { _bookings.remove(booking) }

    fun getBookings(): List<Booking> = _bookings.toList()
}
