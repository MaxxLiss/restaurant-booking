package ru.misis.booking.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.misis.booking.dto.*
import ru.misis.booking.service.RestaurantService
import java.time.LocalDateTime

@Tag(name = "Restaurants", description = "Управление ресторанами, столиками и меню")
@RestController
@RequestMapping("/api/restaurants")
class RestaurantController(private val restaurantService: RestaurantService) {

    @Operation(summary = "Создать ресторан")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateRestaurantRequest): CreateRestaurantResponse =
        restaurantService.createRestaurant(request)

    @Operation(summary = "Список всех ресторанов")
    @GetMapping
    fun getAll(): List<RestaurantSummaryResponse> = restaurantService.getAllRestaurants()

    @Operation(summary = "Получить ресторан по ID")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): RestaurantDetailsResponse = restaurantService.getRestaurant(id)

    @Operation(summary = "Добавить столик")
    @PostMapping("/{id}/tables")
    @ResponseStatus(HttpStatus.CREATED)
    fun addTable(@PathVariable id: Long, @Valid @RequestBody request: CreateTableRequest): CreateTableResponse =
        restaurantService.addTable(id, request)

    @Operation(summary = "Список столиков")
    @GetMapping("/{id}/tables")
    fun getTables(@PathVariable id: Long): List<TableSummaryResponse> = restaurantService.getTables(id)

    @Operation(summary = "Доступные столики на интервал времени")
    @GetMapping("/{id}/tables/available")
    fun getAvailableTables(
        @PathVariable id: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startAt: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endAt: LocalDateTime
    ): List<AvailableTableResponse> = restaurantService.getAvailableTables(id, startAt, endAt)

    @Operation(summary = "Меню ресторана")
    @GetMapping("/{id}/menu")
    fun getMenu(@PathVariable id: Long): MenuDetailsResponse = restaurantService.getMenu(id)

    @Operation(summary = "Добавить блюдо в меню")
    @PostMapping("/{id}/menu/dishes")
    @ResponseStatus(HttpStatus.CREATED)
    fun addDish(@PathVariable id: Long, @Valid @RequestBody request: AddDishRequest): CreateDishResponse =
        restaurantService.addDish(id, request)
}
