package com.studentmanagement.service;

import com.studentmanagement.dto.AuthResponse;
import com.studentmanagement.repository.UserRepository;
import io.micronaut.security.token.generator.TokenGenerator;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TokenGenerator tokenGenerator;

    public UserService(UserRepository userRepository, TokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
    }

    // ИСПРАВЛЕНИЕ 7: убран неиспользуемый import ClaimsGenerator
    public Optional<AuthResponse> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .flatMap(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("sub", username);
                    claims.put("roles", user.getRoles());
                    claims.put("userId", user.getId());
                    claims.put("fullName", user.getFullName());

                    return tokenGenerator.generateToken(claims)
                            .map(token -> new AuthResponse(
                                    token,
                                    username,
                                    user.getRoles(),
                                    user.getId(),
                                    user.getFullName()
                            ));
                });
    }
}
