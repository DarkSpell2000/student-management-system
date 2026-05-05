# Student Management System

REST API на **Spring Boot 3.2** + **PostgreSQL** + **JWT** + **Swagger UI**.

Почему Spring Boot вместо Micronaut:
- Единый parent BOM — нет конфликтов версий annotation processors
- Maven Central + Spring repo доступны из РФ без VPN
- BCrypt из коробки (Micronaut: plaintext в исходном коде)
- Значительно больше документации на русском языке

---

## Быстрый старт

### 1. Запустить PostgreSQL

```bash
docker run -d \
  --name student-db \
  -e POSTGRES_DB=student_management \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### 2. Если Maven Central недоступен (РФ) — добавить зеркало в `~/.m2/settings.xml`:

```xml
<settings>
  <mirrors>
    <mirror>
      <id>yandex</id>
      <mirrorOf>central</mirrorOf>
      <url>https://mirror.yandex.ru/mirrors/maven/</url>
    </mirror>
  </mirrors>
</settings>
```

### 3. Собрать и запустить

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

### 4. Swagger UI

http://localhost:8080/swagger-ui.html

Нажать **Authorize** → вставить токен из `/api/auth/login`.

---

## Дефолтные пользователи (создаются автоматически)

| Логин     | Пароль      | Роль          |
|-----------|-------------|---------------|
| admin     | admin123    | ROLE_ADMIN    |
| curator1  | curator123  | ROLE_CURATOR  |

Группа **ИТ-21** прикреплена к curator1.
Пароли хранятся в **BCrypt** (не plaintext).

---

## Запуск тестов

```bash
# Все тесты
mvn test

# Только юнит-тесты сервиса
mvn test -Dtest=StudentServiceTest

# Только тесты JWT
mvn test -Dtest=JwtTokenProviderTest

# Только интеграционный тест login
mvn test -Dtest=AuthControllerIntegrationTest

# С отчётом покрытия
mvn test
```

Тесты используют **H2 in-memory БД** — PostgreSQL для тестов запускать не нужно.
Конфигурация тестов: `src/test/resources/application.yml`.

### Что тестируется

| Класс | Что проверяет |
|---|---|
| `StudentServiceTest` | Куратор не может видеть/менять чужих студентов; дублирование зачётки; поля `status` и `note` сохраняются; defaultный статус ACTIVE |
| `JwtTokenProviderTest` | Генерация токена, извлечение username/ролей, отклонение чужой подписи и истёкших токенов |
| `AuthControllerIntegrationTest` | HTTP-вход в систему через MockMvc — корректные/неверные учётки, пустое тело |

---

## API эндпоинты

### Аутентификация
| Метод | URL | Доступ | Описание |
|-------|-----|--------|----------|
| POST | `/api/auth/login` | Public | Получить JWT |

### Группы
| Метод | URL | Доступ | Описание |
|-------|-----|--------|----------|
| GET | `/api/groups` | Auth | Все группы |
| GET | `/api/groups/{id}` | Auth | Группа по ID |
| GET | `/api/groups/number/{номер}` | Auth | Группа по номеру |
| POST | `/api/groups` | ADMIN | Создать группу |
| PUT | `/api/groups/{id}` | ADMIN | Обновить группу |
| DELETE | `/api/groups/{id}` | ADMIN | Удалить группу (только пустые) |

### Студенты
| Метод | URL | Доступ | Описание |
|-------|-----|--------|----------|
| GET | `/api/students` | Auth | Все / своя группа |
| GET | `/api/students/{id}` | Auth | По ID (с проверкой прав) |
| GET | `/api/students/group/{id}` | Auth | По группе |
| POST | `/api/students` | Auth | Создать (куратор — только свою группу) |
| PUT | `/api/students/{id}` | Auth | Обновить (с проверкой прав) |
| DELETE | `/api/students/{id}` | Auth | Удалить (с проверкой прав) |

### Поля студента (StudentDto)
- `firstName`, `lastName`, `patronymic` — ФИО
- `birthDate` — дата рождения
- `phoneNumber`, `email`, `address` — контакты
- `recordBookNumber` — номер зачётки (уникальный)
- `status` — `ACTIVE` / `ACADEMIC` / `EXPELLED` / `GRADUATE`
- `note` — заметка куратора (до 1000 символов)
- `groupId` — ID группы

---

## Переменные окружения

| Переменная | По умолчанию | Описание |
|---|---|---|
| `JDBC_URL` | `jdbc:postgresql://localhost:5432/student_management` | URL БД |
| `JDBC_USER` | `postgres` | Пользователь БД |
| `JDBC_PASSWORD` | `postgres` | Пароль БД |
| `JWT_SECRET` | `pleaseChange...` | Секрет для подписи JWT (256+ бит) |
| `JWT_EXPIRATION` | `3600000` | Время жизни токена в мс (1 час) |
| `SERVER_PORT` | `8080` | Порт сервера |
