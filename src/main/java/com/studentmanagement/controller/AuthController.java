package com.studentmanagement.controller;

import com.studentmanagement.dto.AuthRequest;
import com.studentmanagement.dto.AuthResponse;
import com.studentmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Получение JWT-токена")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Войти в систему",
        description = "Возвращает JWT Bearer токен. Используйте его в кнопке Authorize ↑"
    )
    @ApiResponse(responseCode = "200", description = "Успешно",
        content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
