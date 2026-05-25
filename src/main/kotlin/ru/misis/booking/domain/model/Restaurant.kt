package ru.misis.booking.domain.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "restaurants")
data class Restaurant(
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val address: String,
    @Column(nullable = false)
    val cuisine: String,
    @Column(nullable = false)
    var rating: Float = 0f,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    val restaurantId: Long = 0
) {
    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _tables: MutableList<RestaurantTable> = mutableListOf()

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "menu_id")
    val menu: Menu = Menu()

    val tables: List<RestaurantTable> get() = _tables

    init {
        require(name.isNotBlank()) { "Название ресторана обязательно" }
        require(address.isNotBlank()) { "Адрес обязателен" }
        require(cuisine.isNotBlank()) { "Тип кухни обязателен" }
        require(rating in 0f..5f) { "Рейтинг должен быть в диапазоне 0..5" }
    }

    fun addTable(table: RestaurantTable) {
        require(_tables.none { it === table }) { "Столик уже добавлен" }
        table.assignTo(this)
        _tables.add(table)
    }

    fun getAvailableTables(date: LocalDate, time: LocalTime): List<RestaurantTable> =
        _tables.filter { it.isAvailable(date, time) }

    fun matchesName(query: String): Boolean =
        name.contains(query, ignoreCase = true) || address.contains(query, ignoreCase = true)

    fun hasCuisine(cuisineQuery: String): Boolean = cuisine.equals(cuisineQuery, ignoreCase = true)

    fun updateRating(newRating: Float) {
        require(newRating in 0f..5f) { "Рейтинг должен быть в диапазоне 0..5" }
        rating = newRating
    }

    fun findTable(tableId: Long): RestaurantTable? = _tables.firstOrNull { it.tableId == tableId }
}
