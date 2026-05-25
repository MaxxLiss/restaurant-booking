package ru.misis.booking.domain

import ru.misis.booking.domain.enums.Role
import ru.misis.booking.domain.model.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class UserTest {

    @Test
    fun `validates email format`() {
        assertThrows<IllegalArgumentException> { User("noatsign", "+79000000000", "hash") }
    }

    @Test
    fun `validates phone format`() {
        assertThrows<IllegalArgumentException> { User("u@mail.com", "8-900", "hash") }
    }

    @Test
    fun `login matches password hash`() {
        val user = User("u@mail.com", "+79000000000", "secret")
        assert(user.login("secret"))
        assert(!user.login("wrong"))
    }

    @Test
    fun `updateProfile changes email and phone`() {
        val user = User("u@mail.com", "+79000000001", "hash")
        user.updateProfile(newEmail = "new@mail.com", newPhone = "+79000000002")
        assert(user.email == "new@mail.com")
        assert(user.phone == "+79000000002")
    }

    @Test
    fun `isAdmin returns true only for ADMIN role`() {
        val admin = User("a@mail.com", "+79000000003", "hash", Role.ADMIN)
        val client = User("c@mail.com", "+79000000004", "hash", Role.CLIENT)
        assert(admin.isAdmin())
        assert(!client.isAdmin())
    }

    // --- Singleton ---

    @Test
    fun `getOrCreateLoyaltyAccount returns non-null account`() {
        val user = User("u@mail.com", "+79000000000", "hash")
        val account = user.getOrCreateLoyaltyAccount()
        assertNotNull(account)
    }

    @Test
    fun `getOrCreateLoyaltyAccount returns same instance on repeated calls`() {
        val user = User("u@mail.com", "+79000000000", "hash")
        val first = user.getOrCreateLoyaltyAccount()
        val second = user.getOrCreateLoyaltyAccount()
        assertSame(first, second)
    }
}
