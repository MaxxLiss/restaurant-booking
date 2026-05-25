package ru.misis.booking.domain.model

import jakarta.persistence.*
import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "bookings")
data class Booking(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    val table: RestaurantTable,
    @Column(nullable = false)
    val date: LocalDate,
    @Column(nullable = false)
    val time: LocalTime,
    @Column(nullable = false)
    val guests: Int,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BookingStatus = BookingStatus.CONFIRMED,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "pre_order_id")
    var preOrder: PreOrder? = null,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "payment_id")
    var payment: Payment? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    val bookingId: Long = 0
) {
    init {
        require(guests > 0) { "Количество гостей должно быть положительным" }
        table.registerBooking(this)
        user.addBooking(this)
    }

    fun confirm() {
        if (status != BookingStatus.CONFIRMED)
            throw BusinessRuleViolationException("Невозможно подтвердить бронь в статусе $status")
    }

    fun cancel() {
        if (status == BookingStatus.CANCELLED) return
        if (status == BookingStatus.EXPIRED)
            throw BusinessRuleViolationException("Невозможно отменить просроченную бронь")
        status = BookingStatus.CANCELLED
        table.releaseBooking(this)
    }

    fun expireIfNoShow(now: LocalDateTime = LocalDateTime.now()) {
        if (status != BookingStatus.CONFIRMED) return
        val bookingStart = LocalDateTime.of(date, time)
        if (Duration.between(bookingStart, now).toMinutes() >= 15) {
            status = BookingStatus.EXPIRED
            table.releaseBooking(this)
        }
    }

    fun attachPreOrder(preOrder: PreOrder) {
        if (status != BookingStatus.CONFIRMED)
            throw BusinessRuleViolationException("Предзаказ возможен только для подтверждённой брони")
        this.preOrder = preOrder
    }

    fun attachPayment(payment: Payment) {
        if (status != BookingStatus.CONFIRMED)
            throw BusinessRuleViolationException("Оплата возможна только для подтверждённой брони")
        this.payment = payment
    }

    fun calculateTotal(): BigDecimal = preOrder?.calculateTotal() ?: BigDecimal.ZERO

    fun isPaid(): Boolean = payment?.status == PaymentStatus.PAID
}
