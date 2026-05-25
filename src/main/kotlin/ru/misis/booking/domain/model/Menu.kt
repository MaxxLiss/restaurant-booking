package ru.misis.booking.domain.model

import ru.misis.booking.domain.exceptions.InvalidArgumentException
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "menus")
class Menu(
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    val menuId: Long = 0
) {
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "menu_id")
    private val _dishes: MutableList<Dish> = mutableListOf()

    val dishes: List<Dish> get() = _dishes

    fun addDish(dish: Dish) {
        require(_dishes.none { it === dish }) { "Блюдо уже в меню" }
        _dishes.add(dish)
        updatedAt = LocalDateTime.now()
    }

    fun removeDish(dishId: Long) {
        val removed = _dishes.removeIf { it.dishId == dishId }
        if (!removed) throw InvalidArgumentException("Блюдо $dishId не найдено в меню")
        updatedAt = LocalDateTime.now()
    }

    fun findDish(dishId: Long): Dish? = _dishes.firstOrNull { it.dishId == dishId }
}

@Entity
@Table(name = "dishes")
class Dish(
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal,
    @Column(nullable = false)
    val category: String,
    @Column(nullable = false)
    var available: Boolean = true,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    val dishId: Long = 0
) {
    init {
        require(name.isNotBlank()) { "Название блюда обязательно" }
        require(category.isNotBlank()) { "Категория блюда обязательна" }
        require(price >= BigDecimal.ZERO) { "Цена не может быть отрицательной" }
    }

    fun updatePrice(newPrice: BigDecimal) {
        if (newPrice < BigDecimal.ZERO) throw InvalidArgumentException("Цена не может быть отрицательной")
        price = newPrice
    }
}
