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

Проверить:
```bash
docker exec -it student-db psql -U postgres -c "\l"
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

Или использовать прокси/VPN только для загрузки зависимостей (`mvn dependency:resolve`).

### 3. Собрать

```bash
mvn clean package -DskipTests
```

### 4. Запустить

```bash
mvn spring-boot:run
```

С кастомными параметрами БД:
```bash
JDBC_URL=jdbc:postgresql://localhost:5432/student_management \
JDBC_USER=postgres \
JDBC_PASSWORD=postgres \
mvn spring-boot:run
```

### 5. Swagger UI

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

## Получение JWT-токена

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Ответ:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```

Использование:
```bash
curl http://localhost:8080/api/groups \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

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

---

## Что исправлено по сравнению с Micronaut-версией

| Проблема (Micronaut) | Решение (Spring Boot) |
|---|---|
| Конфликт версий: BOM 4.4.0, processors 4.7.0 | Единый spring-boot-starter-parent BOM |
| Пароли в plaintext | BCryptPasswordEncoder |
| Отсутствующий SwaggerSecurityRule.java | @SecurityRequirement + permitAll в SecurityConfig |
| Два дублирующихся application.yml | Один файл в src/main/resources |
| Нет @Transactional на сервисах | @Transactional на всех сервисах |
| Repo недоступны из РФ (repo.micronaut.io) | Maven Central + Spring repos |
| N+1 при getStudentCount() | @Query countStudentsByGroupId |
| Нет глобального error handler | @RestControllerAdvice |

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

---

## Частые проблемы

| Проблема | Решение |
|---|---|
| `Connection refused` к БД | Проверить `docker ps`, порт 5432 |
| Maven не скачивает зависимости | Добавить Yandex mirror в settings.xml |
| Swagger UI — 401 | Нажать Authorize, вставить токен без слова "Bearer" |
| JWT недействителен после перезапуска | Не меняйте JWT_SECRET между запусками |
