package ru.misis.booking.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.misis.booking.dto.*
import ru.misis.booking.service.ReservationService

@Tag(name = "Bookings", description = "Бронирование столиков, предзаказы и оплата")
@RestController
@RequestMapping("/api/bookings")
class BookingController(private val reservationService: ReservationService) {

    @Operation(summary = "Создать бронирование")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateBookingRequest): CreateBookingResponse =
        reservationService.createBooking(request)

    @Operation(summary = "Детали бронирования")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): BookingDetailsResponse = reservationService.getBooking(id)

    @Operation(summary = "Отменить бронирование")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable id: Long) = reservationService.cancelBooking(id)

    @Operation(summary = "Добавить позиции предзаказа")
    @PostMapping("/{id}/pre-order")
    @ResponseStatus(HttpStatus.CREATED)
    fun addPreOrder(
        @PathVariable id: Long,
        @Valid @RequestBody items: List<AddPreOrderItemRequest>
    ): PreOrderResponse = reservationService.addPreOrderItems(id, items)

    @Operation(summary = "Оплатить бронирование")
    @PostMapping("/{id}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    fun pay(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreatePaymentRequest
    ): PaymentResponse = reservationService.processPayment(id, request)
}
