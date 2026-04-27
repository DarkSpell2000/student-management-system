package com.studentmanagement.config;

import com.studentmanagement.security.JwtAuthenticationFilter;
import com.studentmanagement.security.JwtTokenProvider;
import com.studentmanagement.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // включает @PreAuthorize, @Secured на методах контроллеров
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtTokenProvider tokenProvider,
                          UserDetailsServiceImpl userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt — стандарт безопасного хранения паролей
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // REST API — CSRF не нужен (stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless — никаких сессий
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Публичные маршруты
                .requestMatchers("/api/auth/**").permitAll()
                // Swagger UI — доступен без токена
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs"
                ).permitAll()
                // Actuator health (опционально)
                .requestMatchers("/actuator/health").permitAll()
                // Всё остальное — только с токеном
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())

            // Наш JWT-фильтр перед стандартным UsernamePassword фильтром
            .addFilterBefore(jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
