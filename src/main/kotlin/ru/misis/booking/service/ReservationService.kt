package ru.misis.booking.service

import org.springframework.stereotype.Service
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.exceptions.InvalidArgumentException
import ru.misis.booking.domain.model.Booking
import ru.misis.booking.domain.model.Payment
import ru.misis.booking.domain.model.PreOrder
import ru.misis.booking.dto.*
import ru.misis.booking.uow.IUnitOfWork

@Service
class ReservationService(private val uow: IUnitOfWork) {

    fun createBooking(request: CreateBookingRequest): CreateBookingResponse =
        uow.execute {
            val user = users.findById(request.userId)
                ?: throw EntityNotFoundException("Пользователь ${request.userId} не найден")
            val restaurant = restaurants.findById(request.restaurantId)
                ?: throw EntityNotFoundException("Ресторан ${request.restaurantId} не найден")
            val table = restaurant.findTable(request.tableId)
                ?: throw EntityNotFoundException("Столик ${request.tableId} не найден в ресторане ${request.restaurantId}")

            val booking = Booking(user, table, request.startAt, request.endAt, request.guests)
            CreateBookingResponse.from(bookings.save(booking))
        }

    fun getBooking(id: Long): BookingDetailsResponse =
        uow.executeReadOnly {
            BookingDetailsResponse.from(findOrThrow(id))
        }

    fun cancelBooking(id: Long): Unit =
        uow.execute {
            val booking = findOrThrow(id)
            booking.cancel()
            bookings.save(booking)
        }

    fun addPreOrderItems(bookingId: Long, items: List<AddPreOrderItemRequest>): PreOrderResponse =
        uow.execute {
            val booking = findOrThrow(bookingId)
            val restaurant = booking.table.restaurant
                ?: throw InvalidArgumentException("Столик не привязан к ресторану")

            if (booking.preOrder == null) booking.preOrder = PreOrder()
            items.forEach { req ->
                val dish = restaurant.menu.findDish(req.dishId)
                    ?: throw EntityNotFoundException("Блюдо ${req.dishId} не найдено в меню")
                booking.preOrder!!.addItem(dish, req.count)
            }

            bookings.save(booking)
            PreOrderResponse.from(booking.preOrder!!)
        }

    fun processPayment(bookingId: Long, request: CreatePaymentRequest): PaymentResponse =
        uow.execute {
            val booking = findOrThrow(bookingId)
            val payment = Payment(booking.calculateTotal(), request.method)
            payment.process()
            booking.attachPayment(payment)
            bookings.save(booking)
            PaymentResponse.from(payment)
        }

    fun getBookingsByUser(userId: Long): List<BookingSummaryResponse> =
        uow.executeReadOnly {
            bookings.findAllByUserId(userId).map { BookingSummaryResponse.from(it) }
        }

    private fun IUnitOfWork.findOrThrow(id: Long): Booking =
        bookings.findById(id) ?: throw EntityNotFoundException("Бронирование $id не найдено")
}
