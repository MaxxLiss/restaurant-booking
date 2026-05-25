package ru.misis.booking.domain

import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.Role
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
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

    private val start = LocalDateTime.of(2026, 6, 20, 19, 0)
    private val end   = LocalDateTime.of(2026, 6, 20, 21, 0)

    @Test
    fun `booking registers itself on table and in user history`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val booking = Booking(user, table, start, end, 2)

        assertEquals(BookingStatus.CONFIRMED, booking.status)
        assertEquals(1, user.getBookingHistory().size)
        assertEquals(1, table.getBookings().size)
    }

    @Test
    fun `booking cannot exceed table capacity`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable(capacity = 2)
        assertThrows<BusinessRuleViolationException> {
            Booking(user, table, start, end, 5)
        }
    }

    @Test
    fun `overlapping booking on same table is rejected`() {
        val u1 = newUser("1")
        val u2 = newUser("2")
        val (_, table) = newRestaurantWithTable()

        Booking(u1, table, start, end, 2)
        assertThrows<BusinessRuleViolationException> {
            // overlaps: 19:30–21:30 intersects 19:00–21:00
            Booking(u2, table, start.plusMinutes(30), end.plusMinutes(30), 2)
        }
    }

    @Test
    fun `non-overlapping booking on same table is accepted`() {
        val u1 = newUser("1")
        val u2 = newUser("2")
        val (_, table) = newRestaurantWithTable()

        Booking(u1, table, start, end, 2)
        // starts exactly when first one ends — no overlap
        Booking(u2, table, end, end.plusHours(2), 2)

        assertEquals(2, table.getBookings().size)
    }

    @Test
    fun `cancelled booking releases the table`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val booking = Booking(user, table, start, end, 2)

        booking.cancel()
        assertEquals(BookingStatus.CANCELLED, booking.status)
        assertTrue(table.isAvailable(start, end))
    }

    @Test
    fun `no-show booking expires after 15 minutes`() {
        val user = newUser()
        val (_, table) = newRestaurantWithTable()
        val booking = Booking(user, table, start, end, 2)

        booking.expireIfNoShow(now = start.plusMinutes(16))
        assertEquals(BookingStatus.EXPIRED, booking.status)
    }
}
