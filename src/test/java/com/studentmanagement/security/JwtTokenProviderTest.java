package com.studentmanagement.security;

import com.studentmanagement.model.User;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты JwtTokenProvider — проверяем что токены создаются и валидируются корректно.
 * Не требует Spring контекста — создаём провайдер вручную.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        // 256-битный секрет (минимум для HS256)
        String secret = "testSecretKeyForUnitTestsItMustBe256BitsLongForHS256AlgorithmTest";
        tokenProvider = new JwtTokenProvider(secret, 3600000L);
    }

    @Test
    @DisplayName("Токен генерируется и валидируется")
    void generateAndValidate_token() {
        Authentication auth = buildAuth("testuser", List.of("ROLE_CURATOR", "ROLE_USER"));

        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Из токена извлекается username")
    void extractsUsername() {
        Authentication auth = buildAuth("ivanov", List.of("ROLE_ADMIN"));

        String token = tokenProvider.generateToken(auth);

        assertEquals("ivanov", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Из токена извлекаются роли")
    void extractsRoles() {
        Authentication auth = buildAuth("admin", List.of("ROLE_ADMIN", "ROLE_USER"));

        String token = tokenProvider.generateToken(auth);
        List<String> roles = tokenProvider.getRolesFromToken(token);

        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    @DisplayName("Невалидный токен отклоняется")
    void invalidToken_rejected() {
        assertFalse(tokenProvider.validateToken("not.a.real.token"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken("aaa.bbb.ccc"));
    }

    @Test
    @DisplayName("Токен подписанный другим секретом отклоняется")
    void tokenWithDifferentSecret_rejected() {
        // Чужой провайдер с другим секретом
        JwtTokenProvider other = new JwtTokenProvider(
                "anotherDifferentSecretKeyThatIs256BitsLongForHS256TokenSigning",
                3600000L);

        Authentication auth = buildAuth("hacker", List.of("ROLE_ADMIN"));
        String foreignToken = other.generateToken(auth);

        // Наш провайдер должен отклонить чужой токен — либо вернуть false,
        // либо бросить SignatureException. И то, и другое — корректное поведение.
        try {
            boolean valid = tokenProvider.validateToken(foreignToken);
            assertFalse(valid, "Чужой токен не должен пройти валидацию");
        } catch (SignatureException e) {
            // Тоже корректное поведение — подпись не совпала
        }
    }

    @Test
    @DisplayName("Истёкший токен отклоняется")
    void expiredToken_rejected() throws InterruptedException {
        // Провайдер с временем жизни 1 миллисекунда
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "testSecretKeyForUnitTestsItMustBe256BitsLongForHS256AlgorithmTest",
                1L);

        Authentication auth = buildAuth("user", List.of("ROLE_USER"));
        String token = shortLived.generateToken(auth);

        // Ждём пока токен протухнет
        Thread.sleep(50);

        assertFalse(shortLived.validateToken(token));
    }

    // ─── Вспомогательные методы ────────────────────────────────────

    private Authentication buildAuth(String username, List<String> roles) {
        User user = new User(username, "hash", "Тест Тестов", username + "@uni.ru");
        user.setId(99L);
        user.setRoles(Set.copyOf(roles));

        UserPrincipal principal = new UserPrincipal(user);
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }
}