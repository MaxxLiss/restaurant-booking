package ru.misis.booking.domain

import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.domain.model.Review
import ru.misis.booking.domain.model.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

class ReviewTest {

    private fun newUser() = User("u@mail.com", "+79000000001", "hash")
    private fun newRestaurant() = Restaurant("Тест", "Москва", "Итальянская", 4.0f)

    @Test
    fun `rating below 1 is rejected`() {
        assertThrows<IllegalArgumentException> { Review(newUser(), newRestaurant(), rating = 0, comment = "ok") }
    }

    @Test
    fun `rating above 5 is rejected`() {
        assertThrows<IllegalArgumentException> { Review(newUser(), newRestaurant(), rating = 6, comment = "ok") }
    }

    @Test
    fun `valid review can be published`() {
        val review = Review(newUser(), newRestaurant(), rating = 5, comment = "Отлично!")
        review.publish()
        assertTrue(review.published)
    }
}
