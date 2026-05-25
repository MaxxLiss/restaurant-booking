package ru.misis.booking.domain.model

import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

// Переходы: PENDING → PAID → REFUNDED, либо PENDING → FAILED
@Entity
@Table(name = "payments")
data class Payment(
    @Column(nullable = false, precision = 12, scale = 2)
    val amount: BigDecimal,
    @Column(nullable = false)
    val method: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,
    @Column(name = "paid_at")
    var paidAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    val paymentId: Long = 0
) {
    init {
        require(amount > BigDecimal.ZERO) { "Сумма платежа должна быть положительной" }
        require(method.isNotBlank()) { "Метод оплаты обязателен" }
    }

    fun process(): Boolean {
        if (status != PaymentStatus.PENDING)
            throw BusinessRuleViolationException("Платёж уже обработан (статус $status)")
        status = PaymentStatus.PAID
        paidAt = LocalDateTime.now()
        return true
    }

    fun fail() {
        if (status != PaymentStatus.PENDING)
            throw BusinessRuleViolationException("Невозможно пометить как failed: статус $status")
        status = PaymentStatus.FAILED
    }

    fun refund(): Boolean {
        if (status != PaymentStatus.PAID)
            throw BusinessRuleViolationException("Возврат возможен только для оплаченного платежа")
        status = PaymentStatus.REFUNDED
        return true
    }
}
