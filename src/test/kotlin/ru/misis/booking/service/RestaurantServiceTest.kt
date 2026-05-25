package ru.misis.booking.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.Dish
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.dto.*
import ru.misis.booking.repository.RestaurantRepository
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class RestaurantServiceTest {

    @Mock
    lateinit var restaurantRepository: RestaurantRepository

    @InjectMocks
    lateinit var service: RestaurantService

    private fun restaurant() = Restaurant("Пиццерия", "Москва", "Итальянская")

    @Test
    fun `createRestaurant returns CreateRestaurantResponse without rating`() {
        `when`(restaurantRepository.save(any())).thenReturn(restaurant())

        val result = service.createRestaurant(CreateRestaurantRequest("Пиццерия", "Москва", "Итальянская"))

        assertEquals("Пиццерия", result.name)
        assertEquals("Москва", result.address)
        // CreateRestaurantResponse has no rating field — compilation check is the test
    }

    @Test
    fun `getAllRestaurants returns summary list with rating`() {
        `when`(restaurantRepository.findAll()).thenReturn(listOf(restaurant()))

        val result = service.getAllRestaurants()

        assertEquals(1, result.size)
        assertEquals(0f, result[0].rating)
    }

    @Test
    fun `getRestaurant returns full details`() {
        val r = restaurant()
        `when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(r))

        val result = service.getRestaurant(1L)

        assertEquals("Москва", result.address)
    }

    @Test
    fun `getRestaurant throws when not found`() {
        `when`(restaurantRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> { service.getRestaurant(99L) }
    }

    @Test
    fun `addTable returns CreateTableResponse without status`() {
        val r = restaurant()
        `when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(r))
        `when`(restaurantRepository.save(any())).thenReturn(r)

        val result = service.addTable(1L, CreateTableRequest(4, 1f, 2f))

        assertEquals(4, result.capacity)
        // CreateTableResponse has no status field
    }

    @Test
    fun `getAvailableTables returns AvailableTableResponse without status`() {
        val r = restaurant()
        r.addTable(ru.misis.booking.domain.model.RestaurantTable(4, 1f, 1f))
        `when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(r))

        val result = service.getAvailableTables(1L, java.time.LocalDateTime.of(2026, 9, 1, 19, 0), java.time.LocalDateTime.of(2026, 9, 1, 21, 0))

        assertEquals(1, result.size)
        // AvailableTableResponse has no status field
    }

    @Test
    fun `addDish returns CreateDishResponse without available flag`() {
        val r = restaurant()
        `when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(r))
        `when`(restaurantRepository.save(any())).thenReturn(r)

        val result = service.addDish(1L, AddDishRequest("Маргарита", BigDecimal("800.00"), "Пицца"))

        assertEquals("Маргарита", result.name)
        // CreateDishResponse has no available field
    }
}
