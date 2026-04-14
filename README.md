# Student Management System

REST API на Micronaut 4 + PostgreSQL + JWT + Swagger UI.

---

## Быстрый старт

### Шаг 1 — Запустить PostgreSQL через Docker

```bash
docker run -d \
  --name student-db \
  -e POSTGRES_DB=student_management \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

Проверить, что БД работает:
```bash
docker exec -it student-db psql -U postgres -c "\l"
```

### Шаг 2 — Собрать проект

```bash
cd student-management
mvn clean package -DskipTests
```

### Шаг 3 — Запустить

```bash
mvn mn:run
```

Или с переменными окружения (если БД не на localhost):

```bash
JDBC_URL=jdbc:postgresql://localhost:5432/student_management \
JDBC_USER=postgres \
JDBC_PASSWORD=postgres \
mvn mn:run
```

### Шаг 4 — Открыть Swagger UI

http://localhost:8080/swagger-ui

В Swagger UI нажать **Authorize**, ввести токен из /api/auth/login.

---

## Дефолтные пользователи (создаются автоматически)

| Логин    | Пароль     | Роль          |
|----------|------------|---------------|
| admin    | admin123   | ROLE_ADMIN    |
| curator1 | curator123 | ROLE_CURATOR  |

Группа **ИТ-21** прикреплена к curator1.

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
  "roles": ["ROLE_ADMIN"]
}
```

Использовать токен:
```bash
curl http://localhost:8080/api/groups \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## Подключение к существующей PostgreSQL (без Docker)

Создать базу данных вручную:
```sql
CREATE DATABASE student_management;
CREATE USER appuser WITH PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE student_management TO appuser;
```

Запустить с нужными параметрами:
```bash
JDBC_URL=jdbc:postgresql://your-host:5432/student_management \
JDBC_USER=appuser \
JDBC_PASSWORD=secret \
mvn mn:run
```

Таблицы создадутся автоматически (hibernate.hbm2ddl.auto=update).

---

## Частые проблемы

| Проблема | Решение |
|---|---|
| Swagger UI — белая страница | Убедитесь что `mvn package` прошёл без ошибок: annotation processor генерирует UI при компиляции |
| `Connection refused` к БД | Проверьте `docker ps`, убедитесь что контейнер запущен |
| `JWT signature does not match` | Не меняйте `JWT_SECRET` между перезапусками — старые токены станут невалидными |
| 403 на `/swagger-ui` | Проверьте что `SwaggerSecurityRule.java` есть в проекте |
