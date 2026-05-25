package ru.misis.booking.domain

import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.CardPayment
import ru.misis.booking.domain.model.CashPayment
import ru.misis.booking.domain.model.Payment
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class PaymentTest {

    // --- Factory Method ---

    @Test
    fun `create returns CardPayment for CARD method`() {
        assertIs<CardPayment>(Payment.create(BigDecimal("1000"), "CARD"))
    }

    @Test
    fun `create returns CardPayment for ONLINE method`() {
        assertIs<CardPayment>(Payment.create(BigDecimal("1000"), "ONLINE"))
    }

    @Test
    fun `create returns CashPayment for CASH method`() {
        assertIs<CashPayment>(Payment.create(BigDecimal("500"), "CASH"))
    }

    @Test
    fun `create is case-insensitive`() {
        assertIs<CardPayment>(Payment.create(BigDecimal("100"), "card"))
        assertIs<CashPayment>(Payment.create(BigDecimal("100"), "cash"))
    }

    @Test
    fun `create throws for unknown method`() {
        assertThrows<IllegalArgumentException> { Payment.create(BigDecimal("100"), "CRYPTO") }
    }

    // --- CardPayment ---

    @Test
    fun `CardPayment transitions PENDING - PAID - REFUNDED`() {
        val payment = Payment.create(BigDecimal("1000"), "CARD")
        assertEquals(PaymentStatus.PENDING, payment.status)
        payment.process()
        assertEquals(PaymentStatus.PAID, payment.status)
        assertNotNull(payment.paidAt)
        payment.refund()
        assertEquals(PaymentStatus.REFUNDED, payment.status)
    }

    @Test
    fun `CardPayment cannot refund unpaid payment`() {
        val payment = Payment.create(BigDecimal("1000"), "CARD")
        assertThrows<BusinessRuleViolationException> { payment.refund() }
    }

    // --- CashPayment ---

    @Test
    fun `CashPayment transitions PENDING - PAID`() {
        val payment = Payment.create(BigDecimal("500"), "CASH")
        payment.process()
        assertEquals(PaymentStatus.PAID, payment.status)
        assertNotNull(payment.paidAt)
    }

    @Test
    fun `CashPayment refund throws because it is manual`() {
        val payment = Payment.create(BigDecimal("500"), "CASH")
        payment.process()
        assertThrows<BusinessRuleViolationException> { payment.refund() }
    }
}
