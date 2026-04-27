package com.studentmanagement.service;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public StudentService(StudentRepository studentRepository,
                          GroupRepository groupRepository,
                          UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Куратор видит только студентов своей группы.
     */
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsForUser(String username) {
        User user = resolveUser(username);
        if (user.getRoles().contains("ROLE_ADMIN")) {
            return getAllStudents();
        }
        // Куратор: найти его группу через GroupRepository
        return groupRepository.findByCurator(user)
                .map(group -> studentRepository.findByGroupId(group.getId())
                        .stream().map(this::toDto).collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public Optional<StudentDto> getStudentById(Long id, String username) {
        User user = resolveUser(username);
        return studentRepository.findById(id)
                .filter(student -> canAccessStudent(user, student))
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByGroup(Long groupId, String username) {
        User user = resolveUser(username);
        if (!user.getRoles().contains("ROLE_ADMIN")) {
            groupRepository.findByCurator(user)
                .filter(g -> g.getId().equals(groupId))
                .orElseThrow(() -> new SecurityException("Нет прав на просмотр этой группы"));
        }
        return studentRepository.findByGroupId(groupId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public StudentDto createStudent(StudentDto dto, String username) {
        User user = resolveUser(username);

        if (!user.getRoles().contains("ROLE_ADMIN")) {
            // Куратор: может добавлять только в свою группу
            groupRepository.findByCurator(user)
                .filter(g -> g.getId().equals(dto.getGroupId()))
                .orElseThrow(() -> new SecurityException(
                    "Куратор может добавлять студентов только в свою группу"));
        }

        if (studentRepository.existsByRecordBookNumber(dto.getRecordBookNumber())) {
            throw new IllegalArgumentException(
                "Студент с номером зачётки '" + dto.getRecordBookNumber() + "' уже существует");
        }

        Student student = toEntity(dto);
        Student saved = studentRepository.save(student);
        log.info("Создан студент: {} (группа id={})", saved.getFullName(), dto.getGroupId());
        return toDto(saved);
    }

    public StudentDto updateStudent(Long id, StudentDto dto, String username) {
        User user = resolveUser(username);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден: " + id));

        if (!canAccessStudent(user, student)) {
            throw new SecurityException("Нет прав для редактирования этого студента");
        }

        // Проверка уникальности зачётки (исключая текущего студента)
        if (studentRepository.existsByRecordBookNumberAndIdNot(dto.getRecordBookNumber(), id)) {
            throw new IllegalArgumentException(
                "Номер зачётки '" + dto.getRecordBookNumber() + "' уже занят");
        }

        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setPatronymic(dto.getPatronymic());
        student.setBirthDate(dto.getBirthDate());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setEmail(dto.getEmail());
        student.setAddress(dto.getAddress());
        student.setRecordBookNumber(dto.getRecordBookNumber());

        if (dto.getGroupId() != null) {
            groupRepository.findById(dto.getGroupId())
                    .ifPresent(student::setGroup);
        }

        return toDto(studentRepository.save(student));
    }

    public void deleteStudent(Long id, String username) {
        User user = resolveUser(username);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден: " + id));

        if (!canAccessStudent(user, student)) {
            throw new SecurityException("Нет прав для удаления этого студента");
        }

        studentRepository.deleteById(id);
        log.info("Удалён студент id={}", id);
    }

    // ── Вспомогательные методы ──────────────────────────────────────

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден: " + username));
    }

    private boolean canAccessStudent(User user, Student student) {
        if (user.getRoles().contains("ROLE_ADMIN")) return true;
        if (student.getGroup() == null) return false;
        return groupRepository.findByCurator(user)
                .map(g -> g.getId().equals(student.getGroup().getId()))
                .orElse(false);
    }

    private StudentDto toDto(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setFirstName(s.getFirstName());
        dto.setLastName(s.getLastName());
        dto.setPatronymic(s.getPatronymic());
        dto.setBirthDate(s.getBirthDate());
        dto.setPhoneNumber(s.getPhoneNumber());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());
        dto.setRecordBookNumber(s.getRecordBookNumber());
        if (s.getGroup() != null) {
            dto.setGroupId(s.getGroup().getId());
            dto.setGroupNumber(s.getGroup().getGroupNumber());
        }
        return dto;
    }

    private Student toEntity(StudentDto dto) {
        Student s = new Student();
        s.setFirstName(dto.getFirstName());
        s.setLastName(dto.getLastName());
        s.setPatronymic(dto.getPatronymic());
        s.setBirthDate(dto.getBirthDate());
        s.setPhoneNumber(dto.getPhoneNumber());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());
        s.setRecordBookNumber(dto.getRecordBookNumber());
        if (dto.getGroupId() != null) {
            groupRepository.findById(dto.getGroupId()).ifPresent(s::setGroup);
        }
        return s;
    }
}
