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
├── uow/                 # Unit of Work (IUnitOfWork, JpaUnitOfWork)
├── repository/
│   ├── BookingRepository.kt     # Доменный интерфейс — используется сервисами/UoW
│   ├── RestaurantRepository.kt  # Доменный интерфейс
│   ├── UserRepository.kt        # Доменный интерфейс
│   └── jpa/
│       ├── JpaBookingRepository.kt      # Spring Data JPA (инфраструктура)
│       ├── JpaRestaurantRepository.kt   # Spring Data JPA
│       ├── JpaUserRepository.kt         # Spring Data JPA
│       ├── BookingRepositoryAdapter.kt  # Адаптер: JPA → доменный интерфейс
│       ├── RestaurantRepositoryAdapter.kt
│       └── UserRepositoryAdapter.kt
├── dto/                 # Request/Response объекты
├── domain/
│   ├── model/           # JPA-сущности (User, Restaurant, Booking, ...)
│   ├── enums/           # Перечисления статусов
│   └── exceptions/      # Доменные исключения
└── exception/           # GlobalExceptionHandler
```

### Зачем два уровня в `repository/`

Сервисы и UoW зависят только от **доменных интерфейсов** (`BookingRepository` и т.д.) — они ничего не знают о Spring Data или JPA. Это позволяет тестировать их с моками без поднятия контекста Spring.

Всё, что связано с JPA, изолировано в `repository/jpa/`:
- `Jpa*Repository` — Spring Data интерфейсы (автоматически реализуются Spring)
- `*RepositoryAdapter` — тонкая обёртка, которая переводит Spring Data API в доменный интерфейс

## Транзакции (Unit of Work)

Все операции записи выполняются через `IUnitOfWork`:

- `uow.execute { }` — транзакция с возможностью записи; при исключении — полный откат
- `uow.executeReadOnly { }` — read-only транзакция

```kotlin
uow.execute {
    val user = users.findById(id) ?: throw EntityNotFoundException(...)
    user.updateProfile(newEmail, newPhone)
    users.save(user)
}
```

## Тесты

```bash
./gradlew test
```

| Слой | Что покрыто |
|------|-------------|
| Доменная модель | `Booking`, `Payment`, `PreOrder`, `Review`, `LoyaltyAccount`, `RestaurantTable`, `User` |
| Сервисы | `BookingService`, `ReservationService`, `RestaurantService`, `UserService` |
| UoW / транзакции | `JpaUnitOfWorkTest` — коммит, откат, атомарность, каскадный откат |
