# Домашнее задание №4

## Описание
Проект реализует аутентификации пользователей с помощью Spring Security.
Он предоставляет регистрацию новых пользователей, вход в систему и токен-based аутентификацию с использованием JWT (JSON Web Tokens). 
Сервис поддерживает три роли пользователей: ADMIN, PREMIUM_USER и GUEST. Данные пользователя включают уникальный ID, пароль, уникальные email и имя пользователя, а также набор ролей.

### Ключевые функции:
- Токен-based Аутентификация: Использует access-токены (короткоживущие) и refresh-токены (долгоживущие) для безопасных сессий.
- Безопасность Пароля: Пароли хэшируются с помощью BCrypt.
- Роли: Пользователи могут иметь несколько ролей, хранящихся как коллекция enum.
- Возможность обновить access-токен с помощью refresh-токена без повторного ввода логина/пароля.
- Поддержка logout с отзывом refresh-токена
- База Данных: Использует H2 (in-memory) для разработки, с JPA для ORM.
- Валидация: Валидация входных данных с Jakarta Validation для полей, таких как сила пароля и формат email.
- Время Жизни Токена: Ограничено (настраиваемо через application.yml)
- У пользователя с ролью ADMIN есть возможность получить всех пользователей, а также удалить пользователя по id.

## Инструкции по настройке

### 1. Клонируйте репозиторий:
```bash
git clone <url-репозитория>
```

### 2. Соберите Проект:
```bash
mvn clean install
```

### 3. Настройте application.yml если необходимо (в src/main/resources).
Текущий application.yml:
```yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: username
    password: password
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect

jwt:
  secret:
    access: vt3fI5kwBmGvDQPcUrNVkO+Iw/XC75OtH7JlcHlsIcQ=
    refresh: 57gzLd0XY1SJl/g29vSC8jT/UmHoLMC+18bwp+LiG0Q=
  expiration-time:
    access: 10
    refresh: 30
```

### 4. Запустите Приложение:
```bash
mvn spring-boot:run
```

## API Эндпоинты

### 1. Регистрация Пользователя (POST /register)
- Тело запроса:
```json
{
  "username": "kirill",
  "password": "Password123",
  "email": "kirill@mail.ru",
  "roles": ["ADMIN"]
}
```
- Валидация:
 - Логин и email уникальны.
 - Пароль: Минимум 8 символов хотя бы одна uppercase, а также хотя бы одна цифра.
 - Роли: Валидные значения enum: ["ADMIN", "PREMIUM_USER", "GUEST"]
- Ответ: 200 OK с объектом User (без пароля).
- Ошибки: 400 Bad Request для валидаций; 409 Conflict для дубликатов.

### 2. Вход (POST /login)
- Тело запроса:
```json
{
  "username": "kirill",
  "password": "Password123"
}
```
- Ответ: 200 OK с AuthenticationResponse:
```json
{
  "roles": ["ADMIN"],
  "accessToken": "access-token-value",
  "refreshToken": "refresh-token-value"
}
```
- Ошибки: 401 Unauthorized для неверных данных.

### 3. Обновление Токена (POST /refresh)
- Тело запроса:
```json
{
  "refreshToken": "refresh-token-value"
}
```
- Ответ: Новый access-токен без повторного логина.
- Логика: Валидирует refresh-токен, проверяет срок/отзыв, генерирует новый access.

### 4. Выход/Отмена Токена (POST /logout)
Headers: ```Authorization: Bearer access-token-value```

- Тело запроса:
```json
{
  "refreshToken": "refresh-token-value"
}
```
- Ответ: 200 OK при успехе.
- Логика: Помечает refresh-токен как аннулированный в БД.

### 6. Получение всех пользователей (GET /admin/users) - только для роли ADMIN:
- Headers: ```Authorization: Bearer access-token-value```
- Ответ: 200 OK со списком пользователей
- Ошибки: 403 Forbidden, если нет роли ADMIN.

### 7. Удаление пользователя (DELETE /admin/users/{id}) - только для роли ADMIN:
- Headers: ```Authorization: Bearer access-token-value```
- Path Variable: {id} — ID пользователя для удаления.
- Ответ: 200 OK с сообщением об успехе
- Ошибки: Ошибки: 403 Forbidden (нет прав), 404 Not Found (пользователь не найден).

