package ru.misis.booking.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

interface PriceCalculator {
    fun calculate(): BigDecimal
}

class BasePriceCalculator(private val amount: BigDecimal) : PriceCalculator {
    init { require(amount >= BigDecimal.ZERO) { "Базовая цена не может быть отрицательной" } }
    override fun calculate() = amount
}

class DiscountDecorator(
    private val base: PriceCalculator,
    private val percent: BigDecimal
) : PriceCalculator {
    init { require(percent in BigDecimal.ZERO..BigDecimal("100")) { "Скидка должна быть в диапазоне 0..100" } }
    override fun calculate(): BigDecimal =
        base.calculate().multiply(BigDecimal.ONE.subtract(percent.divide(BigDecimal("100"))))
}

class TaxDecorator(
    private val base: PriceCalculator,
    private val percent: BigDecimal
) : PriceCalculator {
    init { require(percent >= BigDecimal.ZERO) { "Налог не может быть отрицательным" } }
    override fun calculate(): BigDecimal =
        base.calculate().multiply(BigDecimal.ONE.add(percent.divide(BigDecimal("100"))))
}

class RoundingDecorator(
    private val base: PriceCalculator,
    private val scale: Int = 2,
    private val mode: RoundingMode = RoundingMode.HALF_UP
) : PriceCalculator {
    override fun calculate(): BigDecimal = base.calculate().setScale(scale, mode)
}
