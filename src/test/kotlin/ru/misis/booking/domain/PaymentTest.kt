package ru.misis.booking.domain

import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.Payment
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PaymentTest {

    @Test
    fun `transitions PENDING - PAID - REFUNDED`() {
        val payment = Payment(BigDecimal("1000"), "CARD")
        assertEquals(PaymentStatus.PENDING, payment.status)
        payment.process()
        assertEquals(PaymentStatus.PAID, payment.status)
        assertNotNull(payment.paidAt)
        payment.refund()
        assertEquals(PaymentStatus.REFUNDED, payment.status)
    }

    @Test
    fun `cannot refund unpaid payment`() {
        val payment = Payment(BigDecimal("1000"), "CARD")
        assertThrows<BusinessRuleViolationException> { payment.refund() }
    }
}
