package ru.misis.booking.service

import org.springframework.stereotype.Service
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.Dish
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.domain.model.RestaurantTable
import ru.misis.booking.dto.*
import ru.misis.booking.uow.IUnitOfWork
import java.time.LocalDateTime

@Service
class RestaurantService(private val uow: IUnitOfWork) {

    fun createRestaurant(request: CreateRestaurantRequest): CreateRestaurantResponse =
        uow.execute {
            val restaurant = Restaurant(request.name, request.address, request.cuisine)
            CreateRestaurantResponse.from(restaurants.save(restaurant))
        }

    fun getAllRestaurants(): List<RestaurantSummaryResponse> =
        uow.executeReadOnly {
            restaurants.findAll().map { RestaurantSummaryResponse.from(it) }
        }

    fun getRestaurant(id: Long): RestaurantDetailsResponse =
        uow.executeReadOnly {
            RestaurantDetailsResponse.from(findOrThrow(id))
        }

    fun addTable(restaurantId: Long, request: CreateTableRequest): CreateTableResponse =
        uow.execute {
            val restaurant = findOrThrow(restaurantId)
            val table = RestaurantTable(request.capacity, request.posX, request.posY)
            restaurant.addTable(table)
            restaurants.save(restaurant)
            CreateTableResponse.from(table)
        }

    fun getTables(restaurantId: Long): List<TableSummaryResponse> =
        uow.executeReadOnly {
            findOrThrow(restaurantId).tables.map { TableSummaryResponse.from(it) }
        }

    fun getAvailableTables(restaurantId: Long, startAt: LocalDateTime, endAt: LocalDateTime): List<AvailableTableResponse> =
        uow.executeReadOnly {
            findOrThrow(restaurantId).getAvailableTables(startAt, endAt).map { AvailableTableResponse.from(it) }
        }

    fun addDish(restaurantId: Long, request: AddDishRequest): CreateDishResponse =
        uow.execute {
            val restaurant = findOrThrow(restaurantId)
            val dish = Dish(request.name, request.price, request.category)
            restaurant.menu.addDish(dish)
            restaurants.save(restaurant)
            CreateDishResponse.from(dish)
        }

    fun getMenu(restaurantId: Long): MenuDetailsResponse =
        uow.executeReadOnly {
            MenuDetailsResponse.from(findOrThrow(restaurantId).menu)
        }

    private fun IUnitOfWork.findOrThrow(id: Long): Restaurant =
        restaurants.findById(id) ?: throw EntityNotFoundException("Ресторан $id не найден")
}
