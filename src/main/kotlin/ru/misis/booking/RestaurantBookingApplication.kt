package ru.misis.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestaurantBookingApplication

fun main(args: Array<String>) {
    runApplication<RestaurantBookingApplication>(*args)
}
