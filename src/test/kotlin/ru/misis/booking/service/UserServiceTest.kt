package ru.misis.booking.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import ru.misis.booking.domain.exceptions.BusinessRuleViolationException
import ru.misis.booking.domain.exceptions.EntityNotFoundException
import ru.misis.booking.domain.model.User
import ru.misis.booking.dto.*
import ru.misis.booking.repository.UserRepository
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @InjectMocks
    lateinit var service: UserService

    @Test
    fun `register returns RegisterResponse without role`() {
        `when`(userRepository.existsByEmail("test@mail.com")).thenReturn(false)
        `when`(userRepository.existsByPhone("+79001234567")).thenReturn(false)
        `when`(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = service.register(RegisterUserRequest("test@mail.com", "+79001234567", "pass"))

        assertEquals("test@mail.com", result.email)
        // RegisterResponse has no role field
    }

    @Test
    fun `register throws when email already taken`() {
        `when`(userRepository.existsByEmail("test@mail.com")).thenReturn(true)

        assertThrows<BusinessRuleViolationException> {
            service.register(RegisterUserRequest("test@mail.com", "+79001234567", "pass"))
        }
    }

    @Test
    fun `register throws when phone already taken`() {
        `when`(userRepository.existsByEmail("any@mail.com")).thenReturn(false)
        `when`(userRepository.existsByPhone("+79001234567")).thenReturn(true)

        assertThrows<BusinessRuleViolationException> {
            service.register(RegisterUserRequest("any@mail.com", "+79001234567", "pass"))
        }
    }

    @Test
    fun `getUser returns UserProfileResponse with role`() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(User("u@mail.com", "+79001234567", "hash")))

        val result = service.getUser(1L)

        assertEquals("u@mail.com", result.email)
        assertNotNull(result.role)  // UserProfileResponse includes role
    }

    @Test
    fun `getUser throws EntityNotFoundException when not found`() {
        `when`(userRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> { service.getUser(99L) }
    }

    @Test
    fun `updateProfile returns UpdateProfileResponse without role`() {
        val user = User("old@mail.com", "+79001234567", "hash")
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = service.updateProfile(1L, UpdateProfileRequest(email = "new@mail.com"))

        assertEquals("new@mail.com", result.email)
        // UpdateProfileResponse has no role field
    }
}
