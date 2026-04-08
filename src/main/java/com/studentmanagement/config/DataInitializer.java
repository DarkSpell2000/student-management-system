package com.studentmanagement.config;

import com.studentmanagement.model.Group;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.UserRepository;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Singleton
public class DataInitializer implements ApplicationEventListener<ServerStartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public DataInitializer(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        // Create default admin if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin123", "Администратор", "admin@university.com");
            admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
            userRepository.save(admin);
            LOG.info("Default admin user created: admin / admin123");
        }

        // Create sample curator and group
        if (!userRepository.existsByUsername("curator1")) {
            User curator = new User("curator1", "curator123", "Иванов Иван Иванович", "ivanov@university.com");
            curator.setRoles(Set.of("ROLE_CURATOR", "ROLE_USER"));
            curator = userRepository.save(curator);

            if (!groupRepository.existsByGroupNumber("ИТ-21")) {
                Group group = new Group("ИТ-21", "Информационных технологий", 2);
                group.setCurator(curator);
                groupRepository.save(group);
                LOG.info("Sample group ИТ-21 created with curator curator1 / curator123");
            }
        }
    }
}
