package ru.misis.booking.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import ru.misis.booking.domain.enums.Channel
import ru.misis.booking.domain.model.User
import ru.misis.booking.notification.EmailTemplate
import ru.misis.booking.notification.PushTemplate
import ru.misis.booking.notification.SmsTemplate
import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {
    @Mock lateinit var userRepo: UserRepository
    @Mock lateinit var restaurantRepo: RestaurantRepository
    @Mock lateinit var bookingRepo: BookingRepository

    lateinit var service: NotificationService

    @BeforeEach
    fun setup() {
        service = NotificationService(TestUnitOfWork(restaurantRepo, userRepo, bookingRepo))
    }

    private fun user(id: Long, email: String) = User(email, "+7900000${id}000", "hash")

    @Test
    fun `notifyAll returns one notification per found user`() {
        val u1 = user(1, "a@mail.com")
        val u2 = user(2, "b@mail.com")
        whenever(userRepo.findById(1L)).thenReturn(u1)
        whenever(userRepo.findById(2L)).thenReturn(u2)

        val result = service.notifyAll(listOf(1L, 2L), EmailTemplate("Столик подтверждён"))

        assertEquals(2, result.size)
        assertEquals(Channel.EMAIL, result[0].channel)
        assertEquals(Channel.EMAIL, result[1].channel)
    }

    @Test
    fun `notifyAll skips missing users`() {
        val u1 = user(1, "a@mail.com")
        whenever(userRepo.findById(1L)).thenReturn(u1)
        whenever(userRepo.findById(99L)).thenReturn(null)

        val result = service.notifyAll(listOf(1L, 99L), SmsTemplate("Текст"))

        assertEquals(1, result.size)
        assertEquals(u1, result[0].user)
    }

    @Test
    fun `notifyAll each notification is an independent clone`() {
        val u1 = user(1, "a@mail.com")
        val u2 = user(2, "b@mail.com")
        whenever(userRepo.findById(1L)).thenReturn(u1)
        whenever(userRepo.findById(2L)).thenReturn(u2)

        val result = service.notifyAll(listOf(1L, 2L), PushTemplate("Новое бронирование"))

        assertNotSame(result[0], result[1])
        assertEquals(u1, result[0].user)
        assertEquals(u2, result[1].user)
    }

    @Test
    fun `notifyAll returns empty list when all users are missing`() {
        whenever(userRepo.findById(99L)).thenReturn(null)

        val result = service.notifyAll(listOf(99L), EmailTemplate("Текст"))

        assertEquals(0, result.size)
    }

    @Test
    fun `notifyAll works with all three template types`() {
        val u = user(1, "a@mail.com")
        whenever(userRepo.findById(1L)).thenReturn(u)

        val email = service.notifyAll(listOf(1L), EmailTemplate("Текст", "Тема"))
        val sms   = service.notifyAll(listOf(1L), SmsTemplate("Текст", 160))
        val push  = service.notifyAll(listOf(1L), PushTemplate("Текст", 50))

        assertEquals(Channel.EMAIL, email[0].channel)
        assertEquals(Channel.SMS,   sms[0].channel)
        assertEquals(Channel.PUSH,  push[0].channel)
    }
}
