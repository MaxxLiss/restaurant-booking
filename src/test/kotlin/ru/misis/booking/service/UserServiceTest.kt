package ru.misis.booking.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.User
import ru.misis.booking.dto.*
import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    @Mock lateinit var restaurantRepo: RestaurantRepository
    @Mock lateinit var userRepo: UserRepository
    @Mock lateinit var bookingRepo: BookingRepository

    lateinit var service: UserService

    @BeforeEach
    fun setup() {
        service = UserService(TestUnitOfWork(restaurantRepo, userRepo, bookingRepo))
    }

    @Test
    fun `register returns RegisterResponse without role`() {
        whenever(userRepo.existsByEmail("test@mail.com")).thenReturn(false)
        whenever(userRepo.existsByPhone("+79001234567")).thenReturn(false)
        whenever(userRepo.save(any())).thenAnswer { it.arguments[0] as User }

        val result = service.register(RegisterUserRequest("test@mail.com", "+79001234567", "pass"))

        assertEquals("test@mail.com", result.email)
    }

    @Test
    fun `register throws when email already taken`() {
        whenever(userRepo.existsByEmail("test@mail.com")).thenReturn(true)

        assertThrows<BusinessRuleViolationException> {
            service.register(RegisterUserRequest("test@mail.com", "+79001234567", "pass"))
        }
    }

    @Test
    fun `register throws when phone already taken`() {
        whenever(userRepo.existsByEmail("any@mail.com")).thenReturn(false)
        whenever(userRepo.existsByPhone("+79001234567")).thenReturn(true)

        assertThrows<BusinessRuleViolationException> {
            service.register(RegisterUserRequest("any@mail.com", "+79001234567", "pass"))
        }
    }

    @Test
    fun `getUser returns UserProfileResponse with role`() {
        whenever(userRepo.findById(1L)).thenReturn(User("u@mail.com", "+79001234567", "hash"))

        val result = service.getUser(1L)

        assertEquals("u@mail.com", result.email)
        assertNotNull(result.role)
    }

    @Test
    fun `getUser throws EntityNotFoundException when not found`() {
        whenever(userRepo.findById(99L)).thenReturn(null)

        assertThrows<EntityNotFoundException> { service.getUser(99L) }
    }

    @Test
    fun `updateProfile returns UpdateProfileResponse without role`() {
        val user = User("old@mail.com", "+79001234567", "hash")
        whenever(userRepo.findById(1L)).thenReturn(user)
        whenever(userRepo.save(any())).thenAnswer { it.arguments[0] as User }

        val result = service.updateProfile(1L, UpdateProfileRequest(email = "new@mail.com"))

        assertEquals("new@mail.com", result.email)
    }
}
