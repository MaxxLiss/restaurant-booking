package ru.misis.booking.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.misis.booking.domain.enums.BookingStatus
import ru.misis.booking.domain.enums.PaymentStatus
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.*
import ru.misis.booking.dto.*
import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ReservationServiceTest {

    @Mock lateinit var restaurantRepo: RestaurantRepository
    @Mock lateinit var userRepo: UserRepository
    @Mock lateinit var bookingRepo: BookingRepository

    lateinit var service: ReservationService

    @BeforeEach
    fun setup() {
        service = ReservationService(TestUnitOfWork(restaurantRepo, userRepo, bookingRepo))
    }

    private val startAt = LocalDateTime.of(2026, 8, 1, 19, 0)
    private val endAt   = LocalDateTime.of(2026, 8, 1, 21, 0)

    private fun buildWorld(): Triple<User, Restaurant, RestaurantTable> {
        val user = User("u@mail.com", "+79001234567", "hash")
        val restaurant = Restaurant("R", "Addr", "C")
        val table = RestaurantTable(4, 0f, 0f)
        restaurant.addTable(table)
        return Triple(user, restaurant, table)
    }

    @Test
    fun `createBooking returns CreateBookingResponse without preOrder and payment`() {
        val (user, restaurant, table) = buildWorld()
        whenever(userRepo.findById(1L)).thenReturn(user)
        whenever(restaurantRepo.findById(1L)).thenReturn(restaurant)
        whenever(bookingRepo.save(any())).thenAnswer { it.arguments[0] as Booking }

        val result = service.createBooking(CreateBookingRequest(1L, 1L, table.tableId, startAt, endAt, 2))

        assertEquals(2, result.guests)
        assertEquals(BookingStatus.CONFIRMED, result.status)
        assertEquals(startAt, result.startAt)
        assertEquals(endAt, result.endAt)
    }

    @Test
    fun `createBooking throws when user not found`() {
        whenever(userRepo.findById(99L)).thenReturn(null)

        assertThrows<EntityNotFoundException> {
            service.createBooking(CreateBookingRequest(99L, 1L, 1L, startAt, endAt, 2))
        }
    }

    @Test
    fun `getBooking returns BookingDetailsResponse with preOrder and payment summary`() {
        val (user, _, table) = buildWorld()
        val booking = Booking(user, table, startAt, endAt, 2)
        whenever(bookingRepo.findById(1L)).thenReturn(booking)

        val result = service.getBooking(1L)

        assertNull(result.preOrder)
        assertNull(result.payment)
        assertEquals(startAt, result.startAt)
    }

    @Test
    fun `cancelBooking saves cancelled booking`() {
        val (user, _, table) = buildWorld()
        val booking = Booking(user, table, startAt, endAt, 2)
        whenever(bookingRepo.findById(1L)).thenReturn(booking)

        service.cancelBooking(1L)

        verify(bookingRepo).save(booking)
        assertEquals(BookingStatus.CANCELLED, booking.status)
    }

    @Test
    fun `addPreOrderItems returns PreOrderResponse with items list`() {
        val (user, restaurant, table) = buildWorld()
        val dish = Dish("Стейк", BigDecimal("1000.00"), "Main")
        restaurant.menu.addDish(dish)
        val booking = Booking(user, table, startAt, endAt, 2)
        whenever(bookingRepo.findById(1L)).thenReturn(booking)
        whenever(bookingRepo.save(any())).thenAnswer { it.arguments[0] as Booking }

        val result = service.addPreOrderItems(1L, listOf(AddPreOrderItemRequest(dish.dishId, 2)))

        assertEquals(1, result.items.size)
        assertEquals(BigDecimal("2000.00"), result.total)
    }

    @Test
    fun `processPayment returns PaymentResponse with PAID status`() {
        val (user, restaurant, table) = buildWorld()
        val dish = Dish("Стейк", BigDecimal("1000.00"), "Main")
        restaurant.menu.addDish(dish)
        val booking = Booking(user, table, startAt, endAt, 2)
        val preOrder = PreOrder().also { it.addItem(dish, 1) }
        booking.preOrder = preOrder
        whenever(bookingRepo.findById(1L)).thenReturn(booking)
        whenever(bookingRepo.save(any())).thenAnswer { it.arguments[0] as Booking }

        val result = service.processPayment(1L, CreatePaymentRequest("CARD"))

        assertEquals(PaymentStatus.PAID, result.status)
        assertEquals(BigDecimal("1000.00"), result.amount)
    }
}
