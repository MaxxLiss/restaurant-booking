package ru.misis.booking.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.Dish
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.domain.model.RestaurantTable
import ru.misis.booking.dto.*
import ru.misis.booking.repository.RestaurantRepository
import java.time.LocalDateTime

@Service
@Transactional
class RestaurantService(private val restaurantRepository: RestaurantRepository) {

    fun createRestaurant(request: CreateRestaurantRequest): CreateRestaurantResponse {
        val restaurant = Restaurant(request.name, request.address, request.cuisine)
        return CreateRestaurantResponse.from(restaurantRepository.save(restaurant))
    }

    @Transactional(readOnly = true)
    fun getAllRestaurants(): List<RestaurantSummaryResponse> =
        restaurantRepository.findAll().map { RestaurantSummaryResponse.from(it) }

    @Transactional(readOnly = true)
    fun getRestaurant(id: Long): RestaurantDetailsResponse =
        RestaurantDetailsResponse.from(findOrThrow(id))

    fun addTable(restaurantId: Long, request: CreateTableRequest): CreateTableResponse {
        val restaurant = findOrThrow(restaurantId)
        val table = RestaurantTable(request.capacity, request.posX, request.posY)
        restaurant.addTable(table)
        restaurantRepository.save(restaurant)
        return CreateTableResponse.from(table)
    }

    @Transactional(readOnly = true)
    fun getTables(restaurantId: Long): List<TableSummaryResponse> =
        findOrThrow(restaurantId).tables.map { TableSummaryResponse.from(it) }

    @Transactional(readOnly = true)
    fun getAvailableTables(restaurantId: Long, startAt: LocalDateTime, endAt: LocalDateTime): List<AvailableTableResponse> =
        findOrThrow(restaurantId).getAvailableTables(startAt, endAt).map { AvailableTableResponse.from(it) }

    fun addDish(restaurantId: Long, request: AddDishRequest): CreateDishResponse {
        val restaurant = findOrThrow(restaurantId)
        val dish = Dish(request.name, request.price, request.category)
        restaurant.menu.addDish(dish)
        restaurantRepository.save(restaurant)
        return CreateDishResponse.from(dish)
    }

    @Transactional(readOnly = true)
    fun getMenu(restaurantId: Long): MenuDetailsResponse =
        MenuDetailsResponse.from(findOrThrow(restaurantId).menu)

    private fun findOrThrow(id: Long): Restaurant =
        restaurantRepository.findById(id).orElseThrow { EntityNotFoundException("Ресторан $id не найден") }
}
