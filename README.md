# Restaurant Booking

REST API для бронирования столиков в ресторане. Spring Boot + Kotlin + H2.

## Стек

- Kotlin 1.9 / JVM 17
- Spring Boot 3.3 (Web, Data JPA, Validation)
- H2 (файловая БД, `./data/booking`)
- Springdoc OpenAPI (Swagger UI)

## Запуск

```bash
./gradlew bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/booking`)

## API

### Users `/api/users`

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/register` | Зарегистрировать пользователя |
| GET | `/{id}` | Профиль пользователя |
| PATCH | `/{id}` | Обновить email / телефон |
| GET | `/{id}/bookings` | История бронирований |

### Restaurants `/api/restaurants`

| Метод | Путь | Описание |
|-------|--|----------|
| POST |  | Создать ресторан |
| GET |  | Список всех ресторанов |
| GET | `/{id}` | Детали ресторана |
| POST | `/{id}/tables` | Добавить столик |
| GET | `/{id}/tables` | Список столиков |
| GET | `/{id}/tables/available` | Свободные столики на интервал (`startAt`, `endAt`) |
| GET | `/{id}/menu` | Меню ресторана |
| POST | `/{id}/menu/dishes` | Добавить блюдо |

### Bookings `/api/bookings`

| Метод | Путь | Описание |
|-------|---|----------|
| POST |  | Создать бронирование |
| GET | `/{id}` | Детали бронирования |
| DELETE | `/{id}` | Отменить бронирование |
| POST | `/{id}/pre-order` | Добавить позиции предзаказа |
| POST | `/{id}/payment` | Оплатить бронирование |

## Структура проекта

```
src/main/kotlin/ru/misis/booking/
├── RestaurantBookingApplication.kt
├── controller/          # REST-контроллеры
├── service/             # Бизнес-логика
├── repository/          # JPA-репозитории
├── dto/                 # Request/Response объекты
├── domain/
│   ├── model/           # JPA-сущности (User, Restaurant, Booking, ...)
│   ├── enums/           # Перечисления статусов
│   └── exceptions/      # Доменные исключения
└── exception/           # GlobalExceptionHandler
```

## Тесты

```bash
./gradlew test
```

Покрыты юнит-тестами: доменная модель (`Booking`, `Payment`, `PreOrder`, `Review`, ...) и сервисы (`BookingService`, `ReservationService`, `RestaurantService`, `UserService`).
