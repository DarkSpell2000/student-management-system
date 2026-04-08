package com.studentmanagement.config;

import com.studentmanagement.repository.UserRepository;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class SecurityConfig implements AuthenticationProvider<HttpRequest<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            @Nullable HttpRequest<?> httpRequest,
            AuthenticationRequest<?, ?> authenticationRequest) {

        String username = authenticationRequest.getIdentity().toString();
        String password = authenticationRequest.getSecret().toString();

        LOG.debug("Authenticating user: {}", username);

        return Flux.create(emitter -> {
            userRepository.findByUsername(username).ifPresentOrElse(
                    user -> {
                        // NOTE: In production, use BCrypt: BCrypt.checkpw(password, user.getPassword())
                        if (user.getPassword().equals(password)) {
                            Map<String, Object> attributes = new HashMap<>();
                            attributes.put("userId", user.getId());
                            attributes.put("fullName", user.getFullName());
                            attributes.put("email", user.getEmail());

                            emitter.next(AuthenticationResponse.success(username, user.getRoles(), attributes));
                            emitter.complete();
                            LOG.info("User authenticated: {}", username);
                        } else {
                            emitter.error(AuthenticationResponse.exception("Invalid credentials"));
                            LOG.warn("Invalid password for user: {}", username);
                        }
                    },
                    () -> {
                        emitter.error(AuthenticationResponse.exception("User not found"));
                        LOG.warn("User not found: {}", username);
                    }
            );
        }, FluxSink.OverflowStrategy.ERROR);
    }
}
