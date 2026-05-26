package ru.misis.booking.domain.model

class RestaurantSearchFilter private constructor(
    val nameLike: String?,
    val cuisine: String?,
    val minRating: Float
) {
    fun matches(restaurant: Restaurant): Boolean {
        if (nameLike != null && !restaurant.matchesName(nameLike)) return false
        if (cuisine != null && !restaurant.hasCuisine(cuisine)) return false
        if (restaurant.rating < minRating) return false
        return true
    }

    class Builder {
        private var nameLike: String? = null
        private var cuisine: String? = null
        private var minRating: Float = 0f

        fun nameLike(name: String) = apply { nameLike = name }
        fun cuisine(cuisine: String) = apply { this.cuisine = cuisine }
        fun minRating(rating: Float) = apply {
            require(rating in 0f..5f) { "Рейтинг должен быть в диапазоне 0..5" }
            minRating = rating
        }

        fun build() = RestaurantSearchFilter(nameLike, cuisine, minRating)
    }
}
