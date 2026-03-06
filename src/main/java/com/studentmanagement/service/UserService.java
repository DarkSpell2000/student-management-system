package com.studentmanagement.service;

import com.studentmanagement.dto.AuthResponse;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;

    public UserService(UserRepository userRepository, JwtTokenGenerator jwtTokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    @Transactional
    public User createUser(String username, String email, String password, String fullName, Set<String> roles) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Optional<AuthResponse> authenticate(String username, String password) {
        LOG.debug("Attempting to authenticate user: {}", username);

        return findByUsername(username)
                .filter(user -> validatePassword(password, user.getPassword()))
                .map(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("username", user.getUsername());
                    claims.put("roles", user.getRoles());
                    claims.put("userId", user.getId());
                    claims.put("fullName", user.getFullName());

                    Authentication authentication = Authentication.build(user.getUsername(), user.getRoles(), claims);

                    return jwtTokenGenerator.generateToken(authentication)
                            .map(token -> new AuthResponse(
                                    token,
                                    user.getUsername(),
                                    user.getRoles(),
                                    user.getId(),
                                    user.getFullName()
                            ))
                            .orElseThrow(() -> new RuntimeException("Failed to generate token"));
                });
    }
}