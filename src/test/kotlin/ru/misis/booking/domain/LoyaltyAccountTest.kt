package ru.misis.booking.domain

import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.model.LoyaltyAccount
import ru.misis.booking.domain.model.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoyaltyAccountTest {

    private fun newUser() = User("u@mail.com", "+79000000001", "hash")

    @Test
    fun `accrues 5 percent bonus and supports redeem`() {
        val account = LoyaltyAccount(newUser())
        account.addBonus(BigDecimal("1000.00"))
        assertEquals(BigDecimal("50.00"), account.balance)
        assertTrue(account.redeem(BigDecimal("30.00")))
        assertEquals(BigDecimal("20.00"), account.balance)
        assertThrows<BusinessRuleViolationException> { account.redeem(BigDecimal("999")) }
    }
}
