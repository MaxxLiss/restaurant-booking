package ru.misis.booking.domain.model

import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

// Переходы: PENDING → PAID → REFUNDED, либо PENDING → FAILED
@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type", discriminatorType = DiscriminatorType.STRING)
abstract class Payment(
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

    abstract fun process(): Boolean

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

    companion object {
        fun create(amount: BigDecimal, method: String): Payment = when (method.uppercase()) {
            "CARD", "ONLINE" -> CardPayment(amount, method)
            "CASH"           -> CashPayment(amount, method)
            else -> throw IllegalArgumentException("Неизвестный метод оплаты: $method")
        }
    }
}

@Entity
@DiscriminatorValue("CARD")
class CardPayment(amount: BigDecimal, method: String) : Payment(amount, method) {
    override fun process(): Boolean {
        if (status != PaymentStatus.PENDING)
            throw BusinessRuleViolationException("Платёж уже обработан (статус $status)")
        status = PaymentStatus.PAID
        paidAt = LocalDateTime.now()
        return true
    }
}

@Entity
@DiscriminatorValue("CASH")
class CashPayment(amount: BigDecimal, method: String) : Payment(amount, method) {
    override fun process(): Boolean {
        if (status != PaymentStatus.PENDING)
            throw BusinessRuleViolationException("Платёж уже обработан (статус $status)")
        status = PaymentStatus.PAID
        paidAt = LocalDateTime.now()
        return true
    }

    override fun refund(): Boolean =
        throw BusinessRuleViolationException("Возврат наличных выполняется вручную вне системы")
}
