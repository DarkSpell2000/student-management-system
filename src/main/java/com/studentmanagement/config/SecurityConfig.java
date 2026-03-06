package com.studentmanagement.config;

import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class SecurityConfig implements AuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                          AuthenticationRequest<?, ?> authenticationRequest) {
        String username = authenticationRequest.getIdentity().toString();
        String password = authenticationRequest.getSecret().toString();

        LOG.debug("Authenticating user: {}", username);

        return Flux.create(emitter -> {
            userRepository.findByUsername(username).ifPresentOrElse(
                    user -> {
                        if (user.getPassword().equals(password)) { // В реальном проекте используйте BCrypt
                            Map<String, Object> attributes = new HashMap<>();
                            attributes.put("user", user);
                            attributes.put("userId", user.getId());
                            attributes.put("fullName", user.getFullName());
                            attributes.put("email", user.getEmail());

                            UserDetails userDetails = new UserDetails(username, user.getRoles(), attributes);
                            emitter.next(userDetails);
                            emitter.complete();
                            LOG.info("User authenticated successfully: {}", username);
                        } else {
                            emitter.error(new AuthenticationException(new AuthenticationFailed("Invalid credentials")));
                            LOG.warn("Invalid password for user: {}", username);
                        }
                    },
                    () -> {
                        emitter.error(new AuthenticationException(new AuthenticationFailed("User not found")));
                        LOG.warn("User not found: {}", username);
                    }
            );
        }, FluxSink.OverflowStrategy.ERROR);
    }
}