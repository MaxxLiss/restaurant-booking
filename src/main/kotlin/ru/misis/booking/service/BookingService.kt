package ru.misis.booking.service

import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BookingService {
    fun createBooking(
        user: User,
        restaurant: Restaurant,
        tableId: Long,
        startAt: LocalDateTime,
        endAt: LocalDateTime,
        guests: Int
    ): Booking {
        val table = restaurant.findTable(tableId)
            ?: throw BusinessRuleViolationException("Столик $tableId не найден в ресторане")
        return Booking(user = user, table = table, startAt = startAt, endAt = endAt, guests = guests)
    }

    fun startPreOrder(booking: Booking): PreOrder {
        booking.preOrder?.let { return it }
        val preOrder = PreOrder()
        booking.attachPreOrder(preOrder)
        return preOrder
    }

    fun payAndAccrueBonus(
        booking: Booking,
        method: String,
        loyaltyAccount: LoyaltyAccount
    ): Payment {
        val preOrder = booking.preOrder
            ?: throw BusinessRuleViolationException("Нельзя оплатить бронь без предзаказа")
        preOrder.ensureNotEmpty()
        val total = booking.calculateTotal()
        val payment = Payment(amount = total, method = method)
        booking.attachPayment(payment)
        payment.process()
        loyaltyAccount.addBonus(total)
        return payment
    }
}
