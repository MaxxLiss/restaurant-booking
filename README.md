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

## Тесты

```bash                                                                                                                                                                                                     
./gradlew test
```

## API

### Users

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/users/register` | Зарегистрировать пользователя |
| GET | `/api/users/{id}` | Профиль пользователя |
| PATCH | `/api/users/{id}` | Обновить email / телефон |
| GET | `/api/users/{id}/bookings` | История бронирований |

### Restaurants

| Метод | Путь | Описание |
|-------|--|----------|
| POST | `/api/restaurants` | Создать ресторан |
| GET | `/api/restaurants` | Список всех ресторанов |
| GET | `/api/restaurants/{id}` | Детали ресторана |
| POST | `/api/restaurants/{id}/tables` | Добавить столик |
| GET | `/api/restaurants/{id}/tables` | Список столиков |
| GET | `/api/restaurants/{id}/tables/available` | Свободные столики на интервал (`startAt`, `endAt`) |
| GET | `/api/restaurants/{id}/menu` | Меню ресторана |
| POST | `/api/restaurants/{id}/menu/dishes` | Добавить блюдо |

### Bookings

| Метод | Путь | Описание |
|-------|---|----------|
| POST | `/api/bookings` | Создать бронирование |
| GET | `/api/bookings/{id}` | Детали бронирования |
| DELETE | `/api/bookings/{id}` | Отменить бронирование |
| POST | `/api/bookings/{id}/pre-order` | Добавить позиции предзаказа |
| POST | `/api/bookings/{id}/payment` | Оплатить бронирование |

## Структура проекта

```
src/main/kotlin/ru/misis/booking/
├── RestaurantBookingApplication.kt
├── controller/          # REST-контроллеры
├── service/
│   ├── BookingService.kt    # Бронирования, предзаказы, оплата + программа лояльности
│   ├── RestaurantService.kt # Рестораны, столики, меню
│   └── UserService.kt       # Пользователи, профиль, история броней
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
│   ├── model/           # Доменные модели: JPA-сущности и чистые объекты (RestaurantSearchFilter, ...)
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

## Программа лояльности

При каждой оплате бронирования автоматически начисляется 5% от суммы заказа на счёт лояльности пользователя. Ответ `POST /{id}/payment` содержит поле `bonusAccrued` с суммой начисленных бонусов.

```
POST /api/bookings/{id}/payment  →  { ..., "bonusAccrued": 150.00 }
```

Начисление происходит в `BookingService.processPayment`:
1. Создаётся/загружается `LoyaltyAccount` через `user.getOrCreateLoyaltyAccount()`
2. Вызывается `loyaltyAccount.addBonus(total)` — начисляет `total × 0.05`
3. Пользователь сохраняется (каскадно сохраняется счёт лояльности)

## Паттерны проектирования

### Singleton — `LoyaltyAccount`

У каждого пользователя ровно один счёт лояльности. Доменный инвариант обеспечивается методом `User.getOrCreateLoyaltyAccount()`: при повторном вызове возвращается тот же экземпляр, а не новый.

```kotlin
val account = user.getOrCreateLoyaltyAccount() // создаёт при первом вызове
val same    = user.getOrCreateLoyaltyAccount() // возвращает тот же объект
```

### Prototype + Abstract Factory — `NotificationTemplate`

`NotificationTemplate` совмещает два паттерна.

**Prototype** — абстрактный класс с тремя наследниками: `EmailTemplate`, `SmsTemplate`, `PushTemplate`. `NotificationService.notifyAll()` принимает один шаблон и список ID пользователей — клонирует шаблон (`copy()`) на каждого пользователя, чтобы изменения конфигурации одного экземпляра не влияли на остальных.

**Abstract Factory** — абстрактный метод `createNotification(user)` инкапсулирует логику создания `Notification`. Каждый подкласс знает, как именно собрать объект-уведомление для своего канала: добавить subject-prefix, обрезать текст и т.д. `NotificationService` работает только с базовым типом и не знает деталей создания.

```kotlin
val template = EmailTemplate("Ваш столик подтверждён", subjectPrefix = "Бронирование")
val notifications = notificationService.notifyAll(listOf(1L, 2L, 3L), template)
// → [Notification(user=1, ...), Notification(user=2, ...), Notification(user=3, ...)]
```

Сервис вызывает `template.copy().createNotification(user)` через базовый тип. Добавление нового канала не затрагивает `NotificationService`.

| Тип | Канал | Поведение `createNotification` |
|-----|-------|--------------------------------|
| `EmailTemplate(subjectPrefix)` | EMAIL | Оборачивает: `[prefix] message` |
| `SmsTemplate(maxLength)` | SMS | Обрезает до `maxLength` символов (по умолчанию 160) |
| `PushTemplate(titleMaxLength)` | PUSH | Обрезает до `titleMaxLength` символов с `…` |

### Builder — `RestaurantSearchFilter`

`RestaurantSearchFilter` строится через вложенный класс `Builder`. Все параметры опциональны: при вызове `build()` без настроек фильтр пропускает все рестораны.

```kotlin
val filter = RestaurantSearchFilter.Builder()
    .nameLike("Рим")
    .cuisine("Итальянская")
    .minRating(4.0f)
    .build()

val restaurants = restaurantService.getAllRestaurants(filter)
```

В контроллере `GET /api/restaurants` принимает query-параметры `name`, `cuisine`, `minRating` и строит фильтр через Builder перед передачей в сервис. Если параметры не переданы — возвращаются все рестораны.

| Метод Builder | Критерий |
|---------------|----------|
| `nameLike(name)` | Подстрока в названии или адресе (без учёта регистра) |
| `cuisine(cuisine)` | Точное совпадение типа кухни (без учёта регистра) |
| `minRating(rating)` | Рейтинг `>= rating`; допустимые значения: 0..5 |

### Factory Method — `Payment`

Метод оплаты определяет поведение платежа. `Payment.create(amount, method)` возвращает нужный подтип:

| Метод | Тип | Возврат |
|-------|-----|---------|
| `CARD`, `ONLINE` | `CardPayment` | поддерживается |
| `CASH` | `CashPayment` | только вручную вне системы |

```kotlin
val payment = Payment.create(total, "CARD")   // → CardPayment
val payment = Payment.create(total, "CASH")   // → CashPayment
payment.process()
```

Хранятся в одной таблице (`SINGLE_TABLE` JPA-наследование), тип фиксируется в колонке `payment_type`.
