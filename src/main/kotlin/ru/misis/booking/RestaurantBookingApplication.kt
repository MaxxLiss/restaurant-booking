package ru.misis.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [
    DataSourceAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class,
])
class RestaurantBookingApplication

fun main(args: Array<String>) {
    runApplication<ru.misis.booking.RestaurantBookingApplication>(*args)
}
