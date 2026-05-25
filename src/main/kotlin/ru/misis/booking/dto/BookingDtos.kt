package ru.misis.booking.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateBookingRequest(
    val userId: Long,
    val restaurantId: Long,
    val tableId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    @field:Min(1) val guests: Int
)

data class AddPreOrderItemRequest(
    val dishId: Long,
    @field:Min(1) val count: Int
)

data class CreatePaymentRequest(
    @field:NotBlank val method: String
)

data class CreateBookingResponse(
    val bookingId: Long,
    val userId: Long,
    val tableId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val guests: Int,
    val status: BookingStatus
) {
    companion object {
        fun from(b: Booking) = CreateBookingResponse(b.bookingId, b.user.userId, b.table.tableId, b.startAt, b.endAt, b.guests, b.status)
    }
}

data class BookingDetailsResponse(
    val bookingId: Long,
    val userId: Long,
    val tableId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val guests: Int,
    val status: BookingStatus,
    val preOrder: PreOrderSummary?,
    val payment: PaymentSummary?,
    val total: BigDecimal
) {
    companion object {
        fun from(b: Booking) = BookingDetailsResponse(
            b.bookingId, b.user.userId, b.table.tableId,
            b.startAt, b.endAt, b.guests, b.status,
            b.preOrder?.let { PreOrderSummary.from(it) },
            b.payment?.let { PaymentSummary.from(it) },
            b.calculateTotal()
        )
    }
}

data class PreOrderSummary(val orderId: Long, val itemCount: Int, val total: BigDecimal) {
    companion object { fun from(p: PreOrder) = PreOrderSummary(p.orderId, p.items.size, p.calculateTotal()) }
}

data class PaymentSummary(val paymentId: Long, val amount: BigDecimal, val status: PaymentStatus) {
    companion object { fun from(p: Payment) = PaymentSummary(p.paymentId, p.amount, p.status) }
}


data class PreOrderItemLine(val itemId: Long, val dishName: String, val count: Int, val unitPrice: BigDecimal, val subtotal: BigDecimal) {
    companion object {
        fun from(i: PreOrderItem) = PreOrderItemLine(i.itemId, i.dish.name, i.count, i.unitPrice, i.getSubtotal())
    }
}

data class PreOrderResponse(val orderId: Long, val items: List<PreOrderItemLine>, val tip: BigDecimal, val discount: BigDecimal, val total: BigDecimal) {
    companion object {
        fun from(p: PreOrder) = PreOrderResponse(p.orderId, p.items.map { PreOrderItemLine.from(it) }, p.tip, p.discount, p.calculateTotal())
    }
}

data class PaymentResponse(
    val paymentId: Long,
    val amount: BigDecimal,
    val method: String,
    val status: PaymentStatus,
    val paidAt: LocalDateTime?,
    val bonusAccrued: BigDecimal = BigDecimal.ZERO
) {
    companion object {
        fun from(p: Payment, bonusAccrued: BigDecimal = BigDecimal.ZERO) =
            PaymentResponse(p.paymentId, p.amount, p.method, p.status, p.paidAt, bonusAccrued)
    }
}
