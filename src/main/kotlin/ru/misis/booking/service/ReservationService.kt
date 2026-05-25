package ru.misis.booking.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.exceptions.InvalidArgumentException
import ru.misis.booking.domain.model.Booking
import ru.misis.booking.domain.model.Payment
import ru.misis.booking.domain.model.PreOrder
import ru.misis.booking.dto.*
import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository

@Service
@Transactional
class ReservationService(
    private val bookingRepository: BookingRepository,
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository
) {

    fun createBooking(request: CreateBookingRequest): CreateBookingResponse {
        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("Пользователь ${request.userId} не найден") }
        val restaurant = restaurantRepository.findById(request.restaurantId)
            .orElseThrow { EntityNotFoundException("Ресторан ${request.restaurantId} не найден") }
        val table = restaurant.findTable(request.tableId)
            ?: throw EntityNotFoundException("Столик ${request.tableId} не найден в ресторане ${request.restaurantId}")

        val booking = Booking(user, table, request.startAt, request.endAt, request.guests)
        return CreateBookingResponse.from(bookingRepository.save(booking))
    }

    @Transactional(readOnly = true)
    fun getBooking(id: Long): BookingDetailsResponse = BookingDetailsResponse.from(findOrThrow(id))

    fun cancelBooking(id: Long) {
        val booking = findOrThrow(id)
        booking.cancel()
        bookingRepository.save(booking)
    }

    fun addPreOrderItems(bookingId: Long, items: List<AddPreOrderItemRequest>): PreOrderResponse {
        val booking = findOrThrow(bookingId)
        val restaurant = booking.table.restaurant
            ?: throw InvalidArgumentException("Столик не привязан к ресторану")
        val menu = restaurant.menu

        if (booking.preOrder == null) booking.preOrder = PreOrder()

        items.forEach { req ->
            val dish = menu.findDish(req.dishId)
                ?: throw EntityNotFoundException("Блюдо ${req.dishId} не найдено в меню")
            booking.preOrder!!.addItem(dish, req.count)
        }

        bookingRepository.save(booking)
        return PreOrderResponse.from(booking.preOrder!!)
    }

    fun processPayment(bookingId: Long, request: CreatePaymentRequest): PaymentResponse {
        val booking = findOrThrow(bookingId)
        val amount = booking.calculateTotal()
        val payment = Payment(amount, request.method)
        payment.process()
        booking.attachPayment(payment)
        bookingRepository.save(booking)
        return PaymentResponse.from(payment)
    }

    @Transactional(readOnly = true)
    fun getBookingsByUser(userId: Long): List<BookingSummaryResponse> =
        bookingRepository.findAllByUserUserId(userId).map { BookingSummaryResponse.from(it) }

    private fun findOrThrow(id: Long): Booking =
        bookingRepository.findById(id).orElseThrow { EntityNotFoundException("Бронирование $id не найдено") }
}
