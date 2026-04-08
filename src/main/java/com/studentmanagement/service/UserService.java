package com.studentmanagement.service;

import com.studentmanagement.dto.AuthResponse;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import io.micronaut.security.token.generator.TokenGenerator;
import io.micronaut.security.token.claims.ClaimsGenerator;
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

    public Optional<AuthResponse> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("sub", username);
                    claims.put("roles", user.getRoles());
                    claims.put("userId", user.getId());
                    claims.put("fullName", user.getFullName());

                    Optional<String> token = tokenGenerator.generateToken(claims);
                    return token.map(t -> new AuthResponse(
                            t,
                            username,
                            user.getRoles(),
                            user.getId(),
                            user.getFullName()
                    )).orElse(null);
                });
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
