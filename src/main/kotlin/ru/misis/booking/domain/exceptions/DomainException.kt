package ru.misis.booking.domain.exceptions

open class DomainException(message: String) : RuntimeException(message)

class BusinessRuleViolationException(message: String) : DomainException(message)

class InvalidArgumentException(message: String) : DomainException(message)

class EntityNotFoundException(message: String) : DomainException(message)
