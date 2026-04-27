package com.studentmanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Swagger UI.
 * Кнопка "Authorize" принимает JWT токен из /api/auth/login.
 * Swagger UI доступен по: http://localhost:8080/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Student Management System API",
        version = "1.0.0",
        description = "REST API для управления данными студентов кураторами групп",
        contact = @Contact(name = "Поддержка", email = "support@university.ru")
    )
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Введите JWT токен, полученный из /api/auth/login"
)
public class OpenApiConfig {
}
