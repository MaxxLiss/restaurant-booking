package ru.misis.booking.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import ru.misis.booking.domain.enums.TableStatus
import ru.misis.booking.domain.model.Dish
import ru.misis.booking.domain.model.Menu
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.domain.model.RestaurantTable
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateRestaurantRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val address: String,
    @field:NotBlank val cuisine: String
)

data class CreateTableRequest(
    @field:Min(1) val capacity: Int,
    val posX: Float = 0f,
    val posY: Float = 0f
)

data class AddDishRequest(
    @field:NotBlank val name: String,
    @field:DecimalMin("0.00") val price: BigDecimal,
    @field:NotBlank val category: String
)

data class CreateRestaurantResponse(val restaurantId: Long, val name: String, val address: String, val cuisine: String) {
    companion object { fun from(r: Restaurant) = CreateRestaurantResponse(r.restaurantId, r.name, r.address, r.cuisine) }
}

data class RestaurantSummaryResponse(val restaurantId: Long, val name: String, val cuisine: String, val rating: Float) {
    companion object { fun from(r: Restaurant) = RestaurantSummaryResponse(r.restaurantId, r.name, r.cuisine, r.rating) }
}

data class RestaurantDetailsResponse(val restaurantId: Long, val name: String, val address: String, val cuisine: String, val rating: Float) {
    companion object { fun from(r: Restaurant) = RestaurantDetailsResponse(r.restaurantId, r.name, r.address, r.cuisine, r.rating) }
}

data class CreateTableResponse(val tableId: Long, val capacity: Int, val posX: Float, val posY: Float) {
    companion object { fun from(t: RestaurantTable) = CreateTableResponse(t.tableId, t.capacity, t.posX, t.posY) }
}

data class TableSummaryResponse(val tableId: Long, val capacity: Int, val posX: Float, val posY: Float, val status: TableStatus) {
    companion object { fun from(t: RestaurantTable) = TableSummaryResponse(t.tableId, t.capacity, t.posX, t.posY, t.status) }
}

data class AvailableTableResponse(val tableId: Long, val capacity: Int, val posX: Float, val posY: Float) {
    companion object { fun from(t: RestaurantTable) = AvailableTableResponse(t.tableId, t.capacity, t.posX, t.posY) }
}

data class CreateDishResponse(val dishId: Long, val name: String, val price: BigDecimal, val category: String) {
    companion object { fun from(d: Dish) = CreateDishResponse(d.dishId, d.name, d.price, d.category) }
}

data class MenuDishItem(val dishId: Long, val name: String, val price: BigDecimal, val category: String, val available: Boolean) {
    companion object { fun from(d: Dish) = MenuDishItem(d.dishId, d.name, d.price, d.category, d.available) }
}

data class MenuDetailsResponse(val menuId: Long, val updatedAt: LocalDateTime, val dishes: List<MenuDishItem>) {
    companion object { fun from(m: Menu) = MenuDetailsResponse(m.menuId, m.updatedAt, m.dishes.map { MenuDishItem.from(it) }) }
}
