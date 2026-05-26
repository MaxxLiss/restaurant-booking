package ru.misis.booking.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.domain.model.RestaurantSearchFilter
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestaurantSearchFilterTest {

    private fun restaurant(
        name: String = "Пиццерия",
        address: String = "Москва",
        cuisine: String = "Итальянская",
        rating: Float = 4.0f
    ): Restaurant {
        val r = Restaurant(name, address, cuisine)
        r.updateRating(rating)
        return r
    }

    @Test
    fun `empty filter matches any restaurant`() {
        val filter = RestaurantSearchFilter.Builder().build()
        assertTrue(filter.matches(restaurant()))
        assertTrue(filter.matches(restaurant(name = "Суши-бар", cuisine = "Японская", rating = 1f)))
    }

    @Test
    fun `nameLike matches by name substring`() {
        val filter = RestaurantSearchFilter.Builder().nameLike("пицц").build()
        assertTrue(filter.matches(restaurant(name = "Пиццерия Рим")))
        assertFalse(filter.matches(restaurant(name = "Суши-бар")))
    }

    @Test
    fun `nameLike matches by address substring`() {
        val filter = RestaurantSearchFilter.Builder().nameLike("арбат").build()
        assertTrue(filter.matches(restaurant(address = "ул. Арбат, 5")))
        assertFalse(filter.matches(restaurant(address = "Тверская, 10")))
    }

    @Test
    fun `cuisine filter is case-insensitive`() {
        val filter = RestaurantSearchFilter.Builder().cuisine("итальянская").build()
        assertTrue(filter.matches(restaurant(cuisine = "Итальянская")))
        assertFalse(filter.matches(restaurant(cuisine = "Японская")))
    }

    @Test
    fun `minRating excludes restaurants below threshold`() {
        val filter = RestaurantSearchFilter.Builder().minRating(4.0f).build()
        assertTrue(filter.matches(restaurant(rating = 4.0f)))
        assertTrue(filter.matches(restaurant(rating = 4.5f)))
        assertFalse(filter.matches(restaurant(rating = 3.9f)))
    }

    @Test
    fun `combined criteria apply AND logic`() {
        val filter = RestaurantSearchFilter.Builder()
            .cuisine("Итальянская")
            .minRating(4.0f)
            .build()
        assertTrue(filter.matches(restaurant(cuisine = "Итальянская", rating = 4.5f)))
        assertFalse(filter.matches(restaurant(cuisine = "Итальянская", rating = 3.0f)))
        assertFalse(filter.matches(restaurant(cuisine = "Японская", rating = 5.0f)))
    }

    @Test
    fun `minRating out of range throws`() {
        assertThrows<IllegalArgumentException> {
            RestaurantSearchFilter.Builder().minRating(6f).build()
        }
        assertThrows<IllegalArgumentException> {
            RestaurantSearchFilter.Builder().minRating(-1f).build()
        }
    }

    @Test
    fun `builder methods are chainable`() {
        val filter = RestaurantSearchFilter.Builder()
            .nameLike("Рим")
            .cuisine("Итальянская")
            .minRating(3.5f)
            .build()
        assertTrue(filter.matches(restaurant(name = "Пиццерия Рим", cuisine = "Итальянская", rating = 4.0f)))
    }
}
