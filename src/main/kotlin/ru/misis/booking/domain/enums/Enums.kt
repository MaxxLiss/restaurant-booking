package ru.misis.booking.domain.enums

enum class BookingStatus { CONFIRMED, CANCELLED, EXPIRED }

enum class TableStatus { FREE, BUSY }

enum class PaymentStatus { PENDING, PAID, REFUNDED, FAILED }

enum class Role { CLIENT, ADMIN }

enum class Channel { PUSH, EMAIL, SMS }
