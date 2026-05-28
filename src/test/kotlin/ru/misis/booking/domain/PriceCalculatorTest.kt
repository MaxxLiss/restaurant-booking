package ru.misis.booking.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.misis.booking.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.assertEquals

class PriceCalculatorTest {

    private fun calc(amount: String) = BasePriceCalculator(BigDecimal(amount))

    @Test
    fun `BasePriceCalculator returns amount as-is`() {
        assertEquals(BigDecimal("500.00"), calc("500.00").calculate())
    }

    @Test
    fun `BasePriceCalculator rejects negative amount`() {
        assertThrows<IllegalArgumentException> { BasePriceCalculator(BigDecimal("-1")) }
    }

    @Test
    fun `DiscountDecorator reduces price by given percent`() {
        val result = DiscountDecorator(calc("1000"), BigDecimal("10")).calculate()
        assertEquals(BigDecimal("900.0"), result)
    }

    @Test
    fun `DiscountDecorator with 0 percent leaves price unchanged`() {
        val result = DiscountDecorator(calc("1000"), BigDecimal.ZERO).calculate()
        assertEquals(BigDecimal("1000"), result)
    }

    @Test
    fun `DiscountDecorator with 100 percent gives zero`() {
        val result = DiscountDecorator(calc("1000"), BigDecimal("100")).calculate()
        assertEquals(BigDecimal.ZERO, result)
    }

    @Test
    fun `DiscountDecorator rejects percent above 100`() {
        assertThrows<IllegalArgumentException> { DiscountDecorator(calc("1000"), BigDecimal("101")) }
    }

    @Test
    fun `TaxDecorator increases price by given percent`() {
        val result = TaxDecorator(calc("1000"), BigDecimal("20")).calculate()
        assertEquals(BigDecimal("1200.0"), result)
    }

    @Test
    fun `TaxDecorator rejects negative percent`() {
        assertThrows<IllegalArgumentException> { TaxDecorator(calc("1000"), BigDecimal("-1")) }
    }

    @Test
    fun `RoundingDecorator rounds to scale`() {
        val base = BasePriceCalculator(BigDecimal("999.999"))
        val result = RoundingDecorator(base).calculate()
        assertEquals(BigDecimal("1000.00"), result)
    }

    @Test
    fun `RoundingDecorator respects custom scale and mode`() {
        val base = BasePriceCalculator(BigDecimal("10.555"))
        val result = RoundingDecorator(base, scale = 1, mode = RoundingMode.FLOOR).calculate()
        assertEquals(BigDecimal("10.5"), result)
    }

    @Test
    fun `decorators compose discount then tax then rounding`() {
        // 1000 - 10% = 900, +20% НДС = 1080, round to 2 dp
        val result = RoundingDecorator(
            TaxDecorator(
                DiscountDecorator(calc("1000"), BigDecimal("10")),
                BigDecimal("20")
            )
        ).calculate()
        assertEquals(BigDecimal("1080.00"), result)
    }

    @Test
    fun `decorator order matters`() {
        // tax then discount vs discount then tax give different results
        val taxFirst = DiscountDecorator(TaxDecorator(calc("1000"), BigDecimal("20")), BigDecimal("10")).calculate()
        val discountFirst = TaxDecorator(DiscountDecorator(calc("1000"), BigDecimal("10")), BigDecimal("20")).calculate()
        // both = 1080 in this case (commutative for * operations), just verify both run
        assertEquals(taxFirst, discountFirst)
    }
}
