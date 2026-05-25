package ru.misis.booking.service

import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookingServiceTest {

    private val service = BookingService()

    private fun newUser(suffix: String = "1") =
        User("u$suffix@mail.com", "+7900000000$suffix", "hash")

    private fun newRestaurantWithTable(capacity: Int = 4): Pair<Restaurant, RestaurantTable> {
        val r = Restaurant("Тестовый", "Москва", "Итальянская", 4.5f)
        val t = RestaurantTable(capacity, 1f, 1f)
        r.addTable(t)
        return r to t
    }

    @Test
    fun `full scenario - book, preorder, pay, accrue bonus`() {
        val user = newUser()
        val (restaurant, table) = newRestaurantWithTable()
        val loyalty = LoyaltyAccount(user)

        val dish = Dish("Стейк", BigDecimal("2000.00"), "Main")
        restaurant.menu.addDish(dish)

        val booking = Booking(user, table, LocalDate.of(2026, 6, 20), LocalTime.of(19, 0), 2)
        val preOrder = service.startPreOrder(booking)
        preOrder.addItem(dish, 2) // 4000

        val payment = service.payAndAccrueBonus(booking, "CARD", loyalty)

        assertEquals(BigDecimal("4000.00"), payment.amount)
        assertTrue(booking.isPaid())
        assertEquals(BigDecimal("200.00"), loyalty.balance)
    }

    @Test
    fun `cannot pay booking without preorder`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val loyalty = LoyaltyAccount(user)
        val booking = Booking(user, table, LocalDate.of(2026, 6, 20), LocalTime.of(19, 0), 2)
        assertThrows<BusinessRuleViolationException> {
            service.payAndAccrueBonus(booking, "CARD", loyalty)
        }
    }

    @Test
    fun `cannot pay booking with empty preorder`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val loyalty = LoyaltyAccount(user)
        val booking = Booking(user, table, LocalDate.of(2026, 6, 20), LocalTime.of(19, 0), 2)
        service.startPreOrder(booking)
        assertThrows<BusinessRuleViolationException> {
            service.payAndAccrueBonus(booking, "CARD", loyalty)
        }
    }
}
