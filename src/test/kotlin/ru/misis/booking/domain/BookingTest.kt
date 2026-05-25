package ru.misis.booking.domain

import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.Role
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookingTest {

    private fun newUser(suffix: String = "1") =
        User("u$suffix@mail.com", "+7900000000$suffix", "hash", Role.CLIENT)

    private fun newRestaurantWithTable(capacity: Int = 4): Pair<Restaurant, RestaurantTable> {
        val r = Restaurant("Тестовый", "Москва", "Итальянская", 4.5f)
        val t = RestaurantTable(capacity, 1f, 1f)
        r.addTable(t)
        return r to t
    }

    @Test
    fun `booking registers itself on table and in user history`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val booking = Booking(user, table, LocalDate.of(2026, 6, 20), LocalTime.of(19, 0), 2)

        assertEquals(BookingStatus.CONFIRMED, booking.status)
        assertEquals(1, user.getBookingHistory().size)
        assertEquals(1, table.getBookings().size)
    }

    @Test
    fun `booking cannot exceed table capacity`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable(capacity = 2)
        assertThrows<BusinessRuleViolationException> {
            Booking(user, table, LocalDate.of(2026, 6, 20), LocalTime.of(19, 0), 5)
        }
    }

    @Test
    fun `double booking on same table-date-time is rejected`() {
        val u1 = newUser("1")
        val u2 = newUser("2")
        val (_, table) = newRestaurantWithTable()
        val date = LocalDate.of(2026, 6, 20)
        val time = LocalTime.of(19, 0)

        Booking(u1, table, date, time, 2)
        assertThrows<BusinessRuleViolationException> {
            Booking(u2, table, date, time, 2)
        }
    }

    @Test
    fun `cancelled booking releases the table`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val date = LocalDate.of(2026, 6, 20)
        val time = LocalTime.of(19, 0)
        val booking = Booking(user, table, date, time, 2)

        booking.cancel()
        assertEquals(BookingStatus.CANCELLED, booking.status)
        assertTrue(table.isAvailable(date, time))
    }

    @Test
    fun `no-show booking expires after 15 minutes`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val date = LocalDate.of(2026, 6, 20)
        val time = LocalTime.of(19, 0)
        val booking = Booking(user, table, date, time, 2)

        booking.expireIfNoShow(now = LocalDateTime.of(date, time.plusMinutes(16)))
        assertEquals(BookingStatus.EXPIRED, booking.status)
    }
}
