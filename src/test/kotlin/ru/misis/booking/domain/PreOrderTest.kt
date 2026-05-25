package ru.misis.booking.domain

import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.Dish
import ru.misis.booking.domain.model.PreOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

class PreOrderTest {

    @Test
    fun `calculates total by formula`() {
        // 2×500 + 1×300 = 1300; tip 0.10 => 1430; discount 30 => 1400
        val dish1 = Dish("Паста", BigDecimal("500.00"), "Main")
        val dish2 = Dish("Салат", BigDecimal("300.00"), "Starter")
        val preOrder = PreOrder(tip = BigDecimal("0.10"), discount = BigDecimal("30.00"))
        preOrder.addItem(dish1, 2)
        preOrder.addItem(dish2, 1)
        assertEquals(BigDecimal("1400.00"), preOrder.calculateTotal())
    }

    @Test
    fun `dish with negative price is rejected`() {
        assertThrows<IllegalArgumentException> { Dish("X", BigDecimal("-1"), "Main") }
    }

    @Test
    fun `unavailable dish cannot be added to preorder`() {
        val dish = Dish("Паста", BigDecimal("500"), "Main", available = false)
        val preOrder = PreOrder()
        assertThrows<BusinessRuleViolationException> { preOrder.addItem(dish, 1) }
    }
}
