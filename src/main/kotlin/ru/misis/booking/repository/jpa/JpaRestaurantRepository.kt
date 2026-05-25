package ru.misis.booking.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import ru.misis.booking.domain.model.Restaurant

interface JpaRestaurantRepository : JpaRepository<Restaurant, Long>
