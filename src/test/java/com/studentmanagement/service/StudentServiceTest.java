package com.studentmanagement.service;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.model.Group;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.StudentStatus;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для StudentService.
 * Используем Mockito, чтобы заменить репозитории моками — не нужна реальная БД.
 *
 * Что тестируем:
 *   - Куратор видит только свою группу
 *   - Куратор не может добавить студента в чужую группу
 *   - Дублирование номера зачётки не допускается
 *   - Поля status и note корректно сохраняются
 *   - canAccessStudent правильно разграничивает права
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private StudentService studentService;

    private User adminUser;
    private User curatorUser;
    private Group curatorGroup;
    private Group otherGroup;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "hash", "Админ", "admin@uni.ru");
        adminUser.setId(1L);
        adminUser.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));

        curatorUser = new User("curator1", "hash", "Иванов И.И.", "ivanov@uni.ru");
        curatorUser.setId(2L);
        curatorUser.setRoles(Set.of("ROLE_CURATOR", "ROLE_USER"));

        curatorGroup = new Group("ИТ-21", "Информатика", 2);
        curatorGroup.setId(10L);
        curatorGroup.setCurator(curatorUser);

        otherGroup = new Group("ПМ-31", "Прикладная математика", 3);
        otherGroup.setId(20L);
    }

    // ─── Проверки прав доступа ─────────────────────────────────────

    @Test
    @DisplayName("Админ имеет доступ к любому студенту")
    void admin_canAccessAnyStudent() {
        Student student = new Student();
        student.setGroup(otherGroup);

        assertTrue(studentService.canAccessStudent(adminUser, student));
    }

    @Test
    @DisplayName("Куратор имеет доступ к студенту своей группы")
    void curator_canAccessStudentInOwnGroup() {
        Student student = new Student();
        student.setGroup(curatorGroup);

        when(groupRepository.findByCurator(curatorUser))
                .thenReturn(Optional.of(curatorGroup));

        assertTrue(studentService.canAccessStudent(curatorUser, student));
    }

    @Test
    @DisplayName("Куратор НЕ имеет доступа к студенту чужой группы")
    void curator_cannotAccessStudentInOtherGroup() {
        Student student = new Student();
        student.setGroup(otherGroup);

        when(groupRepository.findByCurator(curatorUser))
                .thenReturn(Optional.of(curatorGroup));

        assertFalse(studentService.canAccessStudent(curatorUser, student));
    }

    @Test
    @DisplayName("Куратор НЕ имеет доступа к студенту без группы")
    void curator_cannotAccessOrphanStudent() {
        Student student = new Student();
        student.setGroup(null);

        assertFalse(studentService.canAccessStudent(curatorUser, student));
    }

    // ─── Создание студента ─────────────────────────────────────────

    @Test
    @DisplayName("Куратор не может добавить студента в чужую группу")
    void curator_cannotCreateStudentInForeignGroup() {
        when(userRepository.findByUsername("curator1"))
                .thenReturn(Optional.of(curatorUser));
        when(groupRepository.findByCurator(curatorUser))
                .thenReturn(Optional.of(curatorGroup));

        StudentDto dto = new StudentDto();
        dto.setFirstName("Пётр");
        dto.setLastName("Петров");
        dto.setRecordBookNumber("ZK-001");
        dto.setGroupId(otherGroup.getId());  // Чужая группа

        SecurityException ex = assertThrows(SecurityException.class,
                () -> studentService.createStudent(dto, "curator1"));

        assertTrue(ex.getMessage().contains("свою группу"));
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Нельзя создать студента с дублирующимся номером зачётки")
    void cannotCreateStudent_withDuplicateRecordBook() {
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminUser));
        when(studentRepository.existsByRecordBookNumber("ZK-001"))
                .thenReturn(true);

        StudentDto dto = new StudentDto();
        dto.setFirstName("Пётр");
        dto.setLastName("Петров");
        dto.setRecordBookNumber("ZK-001");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.createStudent(dto, "admin"));

        assertTrue(ex.getMessage().contains("уже существует"));
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Поля status и note корректно сохраняются при создании")
    void createStudent_persistsStatusAndNote() {
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminUser));
        when(studentRepository.existsByRecordBookNumber("ZK-002"))
                .thenReturn(false);
        when(groupRepository.findById(curatorGroup.getId()))
                .thenReturn(Optional.of(curatorGroup));
        // save возвращает то, что ему передали (имитация JPA)
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StudentDto dto = new StudentDto();
        dto.setFirstName("Анна");
        dto.setLastName("Сидорова");
        dto.setRecordBookNumber("ZK-002");
        dto.setGroupId(curatorGroup.getId());
        dto.setStatus(StudentStatus.ACADEMIC);
        dto.setNote("Академический отпуск с 01.09");

        StudentDto result = studentService.createStudent(dto, "admin");

        assertEquals(StudentStatus.ACADEMIC, result.getStatus());
        assertEquals("Академический отпуск с 01.09", result.getNote());
    }

    @Test
    @DisplayName("При создании без статуса студент получает ACTIVE по умолчанию")
    void createStudent_defaultsToActiveStatus() {
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminUser));
        when(studentRepository.existsByRecordBookNumber(any()))
                .thenReturn(false);
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StudentDto dto = new StudentDto();
        dto.setFirstName("Иван");
        dto.setLastName("Иванов");
        dto.setRecordBookNumber("ZK-003");
        // status НЕ задан

        StudentDto result = studentService.createStudent(dto, "admin");

        assertEquals(StudentStatus.ACTIVE, result.getStatus());
    }

    // ─── Получение списка студентов ───────────────────────────────

    @Test
    @DisplayName("Куратор получает только студентов своей группы")
    void curator_getsOnlyOwnGroupStudents() {
        Student s1 = new Student();
        s1.setId(100L);
        s1.setFirstName("А");
        s1.setLastName("А");
        s1.setRecordBookNumber("ZK-A");
        s1.setGroup(curatorGroup);

        when(userRepository.findByUsername("curator1"))
                .thenReturn(Optional.of(curatorUser));
        when(groupRepository.findByCurator(curatorUser))
                .thenReturn(Optional.of(curatorGroup));
        when(studentRepository.findByGroupId(curatorGroup.getId()))
                .thenReturn(List.of(s1));

        List<StudentDto> result = studentService.getStudentsForUser("curator1");

        assertEquals(1, result.size());
        assertEquals("А", result.get(0).getFirstName());
        // findAll НЕ должен вызываться — куратор не имеет доступа ко всем
        verify(studentRepository, never()).findAll();
    }

    @Test
    @DisplayName("Куратор без группы получает пустой список")
    void curator_withoutGroup_getsEmptyList() {
        when(userRepository.findByUsername("curator1"))
                .thenReturn(Optional.of(curatorUser));
        when(groupRepository.findByCurator(curatorUser))
                .thenReturn(Optional.empty());

        List<StudentDto> result = studentService.getStudentsForUser("curator1");

        assertTrue(result.isEmpty());
    }
}
