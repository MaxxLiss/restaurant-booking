package ru.misis.booking.repository

import ru.misis.booking.domain.model.Restaurant

interface RestaurantRepository {
    fun findById(id: Long): Restaurant?
    fun findAll(): List<Restaurant>
    fun save(restaurant: Restaurant): Restaurant
}
