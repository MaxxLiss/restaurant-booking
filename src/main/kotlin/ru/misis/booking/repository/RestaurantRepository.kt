package ru.misis.booking.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.misis.booking.domain.model.Restaurant

interface RestaurantRepository : JpaRepository<Restaurant, Long>
