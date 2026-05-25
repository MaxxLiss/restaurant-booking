package ru.misis.booking.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
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
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReservationServiceTest {

    @Mock lateinit var bookingRepository: BookingRepository
    @Mock lateinit var restaurantRepository: RestaurantRepository
    @Mock lateinit var userRepository: UserRepository

    @InjectMocks
    lateinit var service: ReservationService

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
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        `when`(bookingRepository.save(any())).thenAnswer { it.arguments[0] as Booking }

        val result = service.createBooking(CreateBookingRequest(1L, 1L, table.tableId, startAt, endAt, 2))

        assertEquals(2, result.guests)
        assertEquals(BookingStatus.CONFIRMED, result.status)
        assertEquals(startAt, result.startAt)
        assertEquals(endAt, result.endAt)
    }

    @Test
    fun `createBooking throws when user not found`() {
        `when`(userRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
            service.createBooking(CreateBookingRequest(99L, 1L, 1L, startAt, endAt, 2))
        }
    }

    @Test
    fun `getBooking returns BookingDetailsResponse with preOrder and payment summary`() {
        val (user, _, table) = buildWorld()
        val booking = Booking(user, table, startAt, endAt, 2)
        `when`(bookingRepository.findById(1L)).thenReturn(Optional.of(booking))

        val result = service.getBooking(1L)

        assertNull(result.preOrder)
        assertNull(result.payment)
        assertEquals(startAt, result.startAt)
    }

    @Test
    fun `cancelBooking saves cancelled booking`() {
        val (user, _, table) = buildWorld()
        val booking = Booking(user, table, startAt, endAt, 2)
        `when`(bookingRepository.findById(1L)).thenReturn(Optional.of(booking))

        service.cancelBooking(1L)

        verify(bookingRepository).save(booking)
        assertEquals(BookingStatus.CANCELLED, booking.status)
    }

    @Test
    fun `addPreOrderItems returns PreOrderResponse with items list`() {
        val (user, restaurant, table) = buildWorld()
        val dish = Dish("Стейк", BigDecimal("1000.00"), "Main")
        restaurant.menu.addDish(dish)
        val booking = Booking(user, table, startAt, endAt, 2)
        `when`(bookingRepository.findById(1L)).thenReturn(Optional.of(booking))
        `when`(bookingRepository.save(any())).thenAnswer { it.arguments[0] as Booking }

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
        `when`(bookingRepository.findById(1L)).thenReturn(Optional.of(booking))
        `when`(bookingRepository.save(any())).thenAnswer { it.arguments[0] as Booking }

        val result = service.processPayment(1L, CreatePaymentRequest("CARD"))

        assertEquals(PaymentStatus.PAID, result.status)
        assertEquals(BigDecimal("1000.00"), result.amount)
    }
}
