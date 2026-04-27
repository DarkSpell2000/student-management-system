package com.studentmanagement.service;

import com.studentmanagement.dto.AuthRequest;
import com.studentmanagement.dto.AuthResponse;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.security.JwtTokenProvider;
import com.studentmanagement.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Сервис аутентификации.
 * AuthenticationManager проверяет логин/пароль через BCrypt,
 * затем генерируем JWT.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        String token = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Получаем роли из БД (актуальные)
        User user = userRepository.findByUsername(principal.getUsername())
            .orElseThrow();

        return new AuthResponse(
            token,
            principal.getUsername(),
            user.getRoles(),
            principal.getId(),
            principal.getFullName()
        );
    }
}
