package com.studentmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.dto.AuthRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.annotation.PostConstruct;
import org.springframework.security.web.FilterChainProxy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционный тест AuthController.
 * Поднимает реальный Spring-контекст + H2 in-memory БД + DataInitializer.
 *
 * Это медленнее юнит-теста, но проверяет что все компоненты
 * (контроллер, security, JWT, репозитории, BCrypt) работают вместе.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @PostConstruct
    void init() {
        // Подключаем security-фильтры — иначе тест пройдёт мимо JWT
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webContext)
                .addFilter(springSecurityFilterChain)
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login с корректными учётками → 200 + JWT")
    void login_withValidCredentials_returnsToken() throws Exception {
        AuthRequest request = new AuthRequest("admin", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/login с неверным паролем → 401")
    void login_withWrongPassword_returns401() throws Exception {
        AuthRequest request = new AuthRequest("admin", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login для несуществующего пользователя → 401")
    void login_unknownUser_returns401() throws Exception {
        AuthRequest request = new AuthRequest("ghost", "anyPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login без тела → 400")
    void login_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Куратор curator1 успешно входит и получает роль ROLE_CURATOR")
    void curatorLogin_returnsCuratorRole() throws Exception {
        AuthRequest request = new AuthRequest("curator1", "curator123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("curator1"))
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem("ROLE_CURATOR")));
    }
}
