package com.studentmanagement.controller;

import com.studentmanagement.dto.AuthRequest;
import com.studentmanagement.dto.AuthResponse;
import com.studentmanagement.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/api/auth")
@Secured(SecurityRule.IS_ANONYMOUS)
@Tag(name = "Аутентификация", description = "Эндпоинты для входа в систему")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Post("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT-токена")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учётные данные"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    public HttpResponse<AuthResponse> login(@Body @Valid AuthRequest request) {
        LOG.info("Login attempt for user: {}", request.getUsername());

        return userService.authenticate(request.getUsername(), request.getPassword())
                .map(HttpResponse::ok)
                .orElseGet(() -> {
                    LOG.warn("Failed login attempt for user: {}", request.getUsername());
                    return HttpResponse.unauthorized();
                });
    }
}
