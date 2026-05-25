package ru.misis.booking.uow

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.misis.booking.domain.model.Restaurant
import ru.misis.booking.domain.model.RestaurantTable
import ru.misis.booking.domain.model.User
import ru.misis.booking.repository.jpa.JpaBookingRepository
import ru.misis.booking.repository.jpa.JpaRestaurantRepository
import ru.misis.booking.repository.jpa.JpaUserRepository

@SpringBootTest
class JpaUnitOfWorkTest {
    @Autowired lateinit var uow: IUnitOfWork
    @Autowired lateinit var userJpa: JpaUserRepository
    @Autowired lateinit var restaurantJpa: JpaRestaurantRepository
    @Autowired lateinit var bookingJpa: JpaBookingRepository

    @BeforeEach
    fun clearDb() {
        bookingJpa.deleteAll()
        userJpa.deleteAll()
        restaurantJpa.deleteAll()
    }

    @Test
    fun `execute commits changes when block succeeds`() {
        uow.execute {
            users.save(User("a@mail.com", "+79001234567", "hash"))
        }

        assertEquals(1, userJpa.count())
    }

    @Test
    fun `execute rolls back when exception is thrown`() {
        assertThrows<RuntimeException> {
            uow.execute {
                users.save(User("a@mail.com", "+79001234567", "hash"))
                throw RuntimeException("simulated failure")
            }
        }

        assertEquals(0, userJpa.count())
    }

    @Test
    fun `execute rolls back all saves atomically on exception`() {
        assertThrows<RuntimeException> {
            uow.execute {
                users.save(User("a@mail.com", "+79001234567", "hash1"))
                users.save(User("b@mail.com", "+79007654321", "hash2"))
                throw RuntimeException("mid-transaction failure")
            }
        }

        assertEquals(0, userJpa.count())
    }

    @Test
    fun `execute rollback does not affect data saved outside transaction`() {
        userJpa.save(User("outside@mail.com", "+79001234567", "hash"))

        assertThrows<RuntimeException> {
            uow.execute {
                users.save(User("inside@mail.com", "+79007654321", "hash"))
                throw RuntimeException("failure inside tx")
            }
        }

        assertEquals(1, userJpa.count())
        assertEquals("outside@mail.com", userJpa.findAll().first().email)
    }

    @Test
    fun `execute rolls back cascaded child entities on exception`() {
        val restaurant = restaurantJpa.save(Restaurant("R", "Addr", "C"))

        assertThrows<RuntimeException> {
            uow.execute {
                val r = restaurants.findById(restaurant.restaurantId)!!
                r.addTable(RestaurantTable(4, 0f, 0f))
                restaurants.save(r)
                throw RuntimeException("rollback cascade test")
            }
        }

        val tableCount = uow.executeReadOnly {
            restaurants.findById(restaurant.restaurantId)!!.tables.size
        }
        assertEquals(0, tableCount)
    }

    @Test
    fun `executeReadOnly returns persisted data correctly`() {
        val saved = userJpa.save(User("a@mail.com", "+79001234567", "hash"))

        val found = uow.executeReadOnly { users.findById(saved.userId) }

        assertNotNull(found)
        assertEquals("a@mail.com", found!!.email)
    }
}
