package ru.misis.booking.domain.model

import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.exceptions.InvalidArgumentException
import jakarta.persistence.*
import java.math.BigDecimal
import java.math.RoundingMode

// Bonus = Total · 0.05
@Entity
@Table(name = "loyalty_accounts")
data class LoyaltyAccount(
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,
    @Column(nullable = false, precision = 12, scale = 2)
    var balance: BigDecimal = BigDecimal.ZERO,
    @Column(name = "total_earned", nullable = false, precision = 12, scale = 2)
    var totalEarned: BigDecimal = BigDecimal.ZERO,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    val accountId: Long = 0
) {
    companion object {
        val BONUS_RATE: BigDecimal = BigDecimal("0.05")
    }

    fun addBonus(orderTotal: BigDecimal) {
        if (orderTotal < BigDecimal.ZERO)
            throw InvalidArgumentException("Сумма заказа не может быть отрицательной")
        val bonus = orderTotal.multiply(BONUS_RATE).setScale(2, RoundingMode.HALF_UP)
        balance = balance.add(bonus)
        totalEarned = totalEarned.add(bonus)
    }

    fun redeem(amount: BigDecimal): Boolean {
        if (amount <= BigDecimal.ZERO)
            throw InvalidArgumentException("Сумма списания должна быть положительной")
        if (amount > balance)
            throw BusinessRuleViolationException("Недостаточно бонусов на счёте")
        balance = balance.subtract(amount)
        return true
    }
}
