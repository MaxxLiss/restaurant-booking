package ru.misis.booking.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.domain.model.RestaurantTable
import ru.misis.booking.dto.*
import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class RestaurantServiceTest {
    @Mock lateinit var restaurantRepo: RestaurantRepository
    @Mock lateinit var userRepo: UserRepository
    @Mock lateinit var bookingRepo: BookingRepository

    lateinit var service: RestaurantService

    @BeforeEach
    fun setup() {
        service = RestaurantService(TestUnitOfWork(restaurantRepo, userRepo, bookingRepo))
    }

    private fun restaurant() = Restaurant("Пиццерия", "Москва", "Итальянская")

    @Test
    fun `createRestaurant returns CreateRestaurantResponse without rating`() {
        val r = restaurant()
        whenever(restaurantRepo.save(any())).thenReturn(r)

        val result = service.createRestaurant(CreateRestaurantRequest("Пиццерия", "Москва", "Итальянская"))

        assertEquals("Пиццерия", result.name)
        assertEquals("Москва", result.address)
    }

    @Test
    fun `getAllRestaurants returns summary list with rating`() {
        whenever(restaurantRepo.findAll()).thenReturn(listOf(restaurant()))

        val result = service.getAllRestaurants()

        assertEquals(1, result.size)
        assertEquals(0f, result[0].rating)
    }

    @Test
    fun `getRestaurant returns full details`() {
        whenever(restaurantRepo.findById(1L)).thenReturn(restaurant())

        val result = service.getRestaurant(1L)

        assertEquals("Москва", result.address)
    }

    @Test
    fun `getRestaurant throws when not found`() {
        whenever(restaurantRepo.findById(99L)).thenReturn(null)

        assertThrows<EntityNotFoundException> { service.getRestaurant(99L) }
    }

    @Test
    fun `addTable returns CreateTableResponse without status`() {
        val r = restaurant()
        whenever(restaurantRepo.findById(1L)).thenReturn(r)
        whenever(restaurantRepo.save(any())).thenReturn(r)

        val result = service.addTable(1L, CreateTableRequest(4, 1f, 2f))

        assertEquals(4, result.capacity)
    }

    @Test
    fun `getAvailableTables returns AvailableTableResponse without status`() {
        val r = restaurant()
        r.addTable(RestaurantTable(4, 1f, 1f))
        whenever(restaurantRepo.findById(1L)).thenReturn(r)

        val result = service.getAvailableTables(
            1L,
            LocalDateTime.of(2026, 9, 1, 19, 0),
            LocalDateTime.of(2026, 9, 1, 21, 0)
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `addDish returns CreateDishResponse without available flag`() {
        val r = restaurant()
        whenever(restaurantRepo.findById(1L)).thenReturn(r)
        whenever(restaurantRepo.save(any())).thenReturn(r)

        val result = service.addDish(1L, AddDishRequest("Маргарита", BigDecimal("800.00"), "Пицца"))

        assertEquals("Маргарита", result.name)
    }
}
