package ru.misis.booking.notification

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.misis.booking.domain.enums.Channel
import ru.misis.booking.domain.model.User
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class NotificationTemplateTest {

    private fun user() = User("u@mail.com", "+79001234567", "hash")

    // --- EmailTemplate ---

    @Test
    fun `EmailTemplate wraps message with subject prefix`() {
        val template = EmailTemplate("Столик подтверждён", subjectPrefix = "Бронирование")
        val notification = template.build(user())
        assertEquals("[Бронирование] Столик подтверждён", notification.message)
        assertEquals(Channel.EMAIL, notification.channel)
    }

    @Test
    fun `EmailTemplate clone is independent instance with same config`() {
        val template = EmailTemplate("Текст", subjectPrefix = "Тема")
        val clone = template.copy() as EmailTemplate
        assertNotSame(template, clone)
        assertEquals(template.subjectPrefix, clone.subjectPrefix)
        assertEquals(template.rawMessage, clone.rawMessage)
    }

    // --- SmsTemplate ---

    @Test
    fun `SmsTemplate passes message unchanged when within limit`() {
        val template = SmsTemplate("Короткое сообщение", maxLength = 160)
        assertEquals("Короткое сообщение", template.build(user()).message)
    }

    @Test
    fun `SmsTemplate truncates message to maxLength`() {
        val long = "А".repeat(200)
        val template = SmsTemplate(long, maxLength = 160)
        val notification = template.build(user())
        assertEquals(160, notification.message.length)
    }

    @Test
    fun `SmsTemplate clone is independent instance with same config`() {
        val template = SmsTemplate("Текст", maxLength = 100)
        val clone = template.copy() as SmsTemplate
        assertNotSame(template, clone)
        assertEquals(template.maxLength, clone.maxLength)
    }

    // --- PushTemplate ---

    @Test
    fun `PushTemplate passes short message unchanged`() {
        val template = PushTemplate("Коротко", titleMaxLength = 50)
        assertEquals("Коротко", template.build(user()).message)
    }

    @Test
    fun `PushTemplate truncates long message with ellipsis`() {
        val long = "А".repeat(100)
        val template = PushTemplate(long, titleMaxLength = 50)
        val message = template.build(user()).message
        assertTrue(message.endsWith("…"))
        assertTrue(message.length <= 51)
    }

    @Test
    fun `PushTemplate clone is independent instance with same config`() {
        val template = PushTemplate("Текст", titleMaxLength = 30)
        val clone = template.copy() as PushTemplate
        assertNotSame(template, clone)
        assertEquals(template.titleMaxLength, clone.titleMaxLength)
    }

    // --- Полиморфизм ---

    @Test
    fun `clone via base type returns correct subtype`() {
        val templates: List<NotificationTemplate> = listOf(
            EmailTemplate("msg"),
            SmsTemplate("msg"),
            PushTemplate("msg")
        )
        val clones = templates.map { it.copy() }
        assertTrue(clones[0] is EmailTemplate)
        assertTrue(clones[1] is SmsTemplate)
        assertTrue(clones[2] is PushTemplate)
    }

    @Test
    fun `each clone produces notification for given user`() {
        val u = user()
        val templates: List<NotificationTemplate> = listOf(
            EmailTemplate("msg"),
            SmsTemplate("msg"),
            PushTemplate("msg")
        )
        templates.forEach { template ->
            assertNotSame(template, template.copy())
            assertEquals(u, template.build(u).user)
        }
    }
}
