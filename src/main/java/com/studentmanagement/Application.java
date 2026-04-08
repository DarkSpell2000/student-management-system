package com.studentmanagement;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Student Management System API",
                version = "1.0.0",
                description = "API для управления данными студентов кураторами групп",
                contact = @Contact(
                        name = "Student Management Team",
                        email = "support@studentmanagement.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Локальный сервер")
        }
)
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
