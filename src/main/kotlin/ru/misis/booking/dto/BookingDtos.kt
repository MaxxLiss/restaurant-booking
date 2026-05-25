package ru.misis.booking.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

// ── Requests ─────────────────────────────────────────────────────────────────

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

// ── Responses ─────────────────────────────────────────────────────────────────

// POST /bookings  (no preOrder/payment — don't exist yet)
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

// GET /bookings/{id}  (full details with embedded summaries)
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

// Embedded in BookingDetailsResponse
data class PreOrderSummary(val orderId: Long, val itemCount: Int, val total: BigDecimal) {
    companion object { fun from(p: PreOrder) = PreOrderSummary(p.orderId, p.items.size, p.calculateTotal()) }
}

data class PaymentSummary(val paymentId: Long, val amount: BigDecimal, val status: PaymentStatus) {
    companion object { fun from(p: Payment) = PaymentSummary(p.paymentId, p.amount, p.status) }
}

// POST /bookings/{id}/pre-order  (full items list)
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

// POST /bookings/{id}/payment
data class PaymentResponse(val paymentId: Long, val amount: BigDecimal, val method: String, val status: PaymentStatus, val paidAt: LocalDateTime?) {
    companion object { fun from(p: Payment) = PaymentResponse(p.paymentId, p.amount, p.method, p.status, p.paidAt) }
}
