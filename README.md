# Лабораторная работа №4 — Реализация доменной модели

Скелет Spring Boot приложения на Kotlin. Доменные классы аннотированы JPA —
схему БД можно получить генерацией Hibernate (`ddl-auto: update`) или
извлечь как SQL-скрипт. Сам датасорс пока не подключён.

## Запуск

```bash
./gradlew bootRun     # запуск приложения (без БД)
./gradlew test        # юнит-тесты доменной модели
```

## Структура

```
src/main/kotlin/com/misis/booking/
├── RestaurantBookingApplication.kt
├── domain/
│   ├── enums/Enums.kt
│   ├── exceptions/DomainException.kt
│   └── model/
│       ├── User.kt
│       ├── Restaurant.kt
│       ├── RestaurantTable.kt   (имя класса не конфликтует с jakarta.persistence.Table)
│       ├── Menu.kt              (Menu + Dish)
│       ├── Booking.kt
│       ├── PreOrder.kt          (PreOrder + PreOrderItem)
│       ├── Payment.kt
│       ├── LoyaltyAccount.kt
│       ├── Notification.kt
│       └── Review.kt
└── service/BookingService.kt
```

## Принципы реализации

- «Толстая модель»: бизнес-правила в сущностях, сервис только оркеструет.
- Инкапсуляция: все изменяемые поля имеют `protected set` (нужен Hibernate
  для рефлексивной записи), изменения извне — только через методы класса.
- Инварианты — в `init {}` и проверках внутри методов.
- Переходы состояний (`Booking`, `Payment`) проверяются перед изменением статуса.
- Коллекции наружу возвращаются защитными копиями (`toList()`).
- Связи: `Restaurant` 1↔* `RestaurantTable`, `User` 1↔* `Booking`,
  `Booking` 1↔0..1 `PreOrder` 1↔* `PreOrderItem`, `Booking` 1↔0..1 `Payment`,
  `Restaurant` 1↔1 `Menu` 1↔* `Dish`, `User` 1↔1 `LoyaltyAccount`.

## Подключение БД (когда понадобится)

1. Добавить драйвер в `build.gradle.kts`, например:
   ```kotlin
   runtimeOnly("org.postgresql:postgresql")
   ```
2. Раскомментировать секцию `datasource`/`jpa` в `application.yml`.
3. Убрать `exclude = [...]` из `@SpringBootApplication`.
4. Добавить интерфейсы-репозитории, например:
   ```kotlin
   interface BookingRepository : JpaRepository<Booking, Long>
   ```
