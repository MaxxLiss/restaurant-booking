package ru.misis.booking.domain.model

import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.exceptions.InvalidArgumentException
import jakarta.persistence.*
import java.math.BigDecimal
import java.math.RoundingMode

// Total = Σ(price · count) · (1 + tip) − discount
@Entity
@Table(name = "pre_orders")
class PreOrder(
    @Column(nullable = false, precision = 6, scale = 4)
    var tip: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false, precision = 12, scale = 2)
    var discount: BigDecimal = BigDecimal.ZERO,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    val orderId: Long = 0
) {
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private val _items: MutableList<PreOrderItem> = mutableListOf()

    val items: List<PreOrderItem> get() = _items

    init {
        require(tip >= BigDecimal.ZERO) { "Чаевые не могут быть отрицательными" }
        require(discount >= BigDecimal.ZERO) { "Скидка не может быть отрицательной" }
    }

    fun addItem(dish: Dish, count: Int) {
        if (!dish.available)
            throw BusinessRuleViolationException("Блюдо '${dish.name}' недоступно")
        if (count <= 0)
            throw InvalidArgumentException("Количество должно быть положительным")
        val existing = _items.firstOrNull { it.dish.dishId == dish.dishId && dish.dishId != 0L }
            ?: _items.firstOrNull { it.dish === dish }
        if (existing != null) {
            existing.increase(count)
        } else {
            _items.add(PreOrderItem(dish = dish, count = count, unitPrice = dish.price))
        }
    }

    fun removeItem(itemId: Long) {
        val removed = _items.removeIf { it.itemId == itemId }
        if (!removed) throw InvalidArgumentException("Позиция $itemId не найдена")
    }

    fun calculateTotal(): BigDecimal {
        val subtotal = _items.map { it.getSubtotal() }.fold(BigDecimal.ZERO, BigDecimal::add)
        val withTip = subtotal.multiply(BigDecimal.ONE.add(tip))
        return withTip.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
    }

    fun ensureNotEmpty() {
        if (_items.isEmpty())
            throw BusinessRuleViolationException("Предзаказ пуст — нельзя оплатить")
    }
}

@Entity
@Table(name = "pre_order_items")
class PreOrderItem(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false)
    val dish: Dish,
    @Column(nullable = false)
    var count: Int,
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    val unitPrice: BigDecimal,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    val itemId: Long = 0
) {
    init {
        require(count > 0) { "Количество должно быть положительным" }
        require(unitPrice >= BigDecimal.ZERO) { "Цена не может быть отрицательной" }
    }

    internal fun increase(delta: Int) {
        require(delta > 0) { "Прирост должен быть положительным" }
        count += delta
    }

    fun getSubtotal(): BigDecimal = unitPrice.multiply(BigDecimal(count))
}
