package com.studentmanagement.service;

import com.studentmanagement.dto.UserDto;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       GroupRepository groupRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getCurators() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("ROLE_CURATOR"))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(this::toDto);
    }

    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException(
                "Пользователь с логином «" + dto.getUsername() + "» уже существует");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Пароль обязателен при создании пользователя");
        }
        if (dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 6 символов");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRoles(buildRoles(dto.getRole()));

        User saved = userRepository.save(user);
        log.info("Создан пользователь: {} ({})", saved.getUsername(), dto.getRole());
        return toDto(saved);
    }

    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        if (!user.getUsername().equals(dto.getUsername())
                && userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException(
                "Пользователь с логином «" + dto.getUsername() + "» уже существует");
        }

        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRoles(buildRoles(dto.getRole()));

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getPassword().length() < 6) {
                throw new IllegalArgumentException("Пароль должен содержать минимум 6 символов");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User saved = userRepository.save(user);
        log.info("Обновлён пользователь: {}", saved.getUsername());
        return toDto(saved);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        // Нельзя удалить единственного администратора
        if (user.getRoles().contains("ROLE_ADMIN")) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().contains("ROLE_ADMIN"))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Нельзя удалить единственного администратора");
            }
        }

        // Отвязать от группы через GroupRepository (избегаем LazyInit)
        groupRepository.findByCurator(user).ifPresent(group -> {
            group.setCurator(null);
            groupRepository.save(group);
            log.info("Куратор снят с группы «{}» перед удалением", group.getGroupNumber());
        });

        userRepository.deleteById(id);
        log.info("Удалён пользователь id={}", id);
    }

    private Set<String> buildRoles(String primaryRole) {
        if ("ROLE_ADMIN".equals(primaryRole)) {
            return Set.of("ROLE_ADMIN", "ROLE_USER");
        }
        return Set.of("ROLE_CURATOR", "ROLE_USER");
    }

    // Используем GroupRepository.findByCurator — безопасно в рамках транзакции
    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRoles().contains("ROLE_ADMIN") ? "ROLE_ADMIN" : "ROLE_CURATOR");

        groupRepository.findByCurator(user).ifPresent(group -> {
            dto.setGroupId(group.getId());
            dto.setGroupNumber(group.getGroupNumber());
            dto.setGroupFaculty(group.getFaculty());
        });

        return dto;
    }
}
