package com.studentmanagement.config;

import com.studentmanagement.model.Group;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Инициализация дефолтных пользователей при первом запуске.
 * Пароли хранятся в BCrypt-хеше — не в plaintext.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           GroupRepository groupRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createAdmin();
        createCuratorWithGroup();
    }

    private void createAdmin() {
        if (userRepository.existsByUsername("admin")) return;

        User admin = new User(
            "admin",
            passwordEncoder.encode("admin123"),   // BCrypt hash
            "Администратор системы",
            "admin@university.ru"
        );
        admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
        userRepository.save(admin);
        log.info("✓ Создан admin / admin123 (ROLE_ADMIN)");
    }

    private void createCuratorWithGroup() {
        if (userRepository.existsByUsername("curator1")) return;

        User curator = new User(
            "curator1",
            passwordEncoder.encode("curator123"),  // BCrypt hash
            "Иванов Иван Иванович",
            "ivanov@university.ru"
        );
        curator.setRoles(Set.of("ROLE_CURATOR", "ROLE_USER"));
        curator = userRepository.save(curator);

        if (!groupRepository.existsByGroupNumber("ИТ-21")) {
            Group group = new Group("ИТ-21", "Информационных технологий", 2);
            group.setCurator(curator);
            groupRepository.save(group);
            log.info("✓ Создана группа ИТ-21, куратор: curator1 / curator123 (ROLE_CURATOR)");
        }
    }
}
