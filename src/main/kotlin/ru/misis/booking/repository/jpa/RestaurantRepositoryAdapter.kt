package ru.misis.booking.repository.jpa

import org.springframework.stereotype.Repository
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.repository.RestaurantRepository

@Repository
class RestaurantRepositoryAdapter(private val jpa: JpaRestaurantRepository) : RestaurantRepository {
    override fun findById(id: Long): Restaurant? = jpa.findById(id).orElse(null)
    override fun findAll(): List<Restaurant> = jpa.findAll()
    override fun save(restaurant: Restaurant): Restaurant = jpa.save(restaurant)
}
