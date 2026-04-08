package com.studentmanagement.service;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.model.Group;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.StudentRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class StudentService {

    private static final Logger LOG = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;

    public StudentService(StudentRepository studentRepository, GroupRepository groupRepository) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
    }

    public List<StudentDto> getAllStudents() {
        List<StudentDto> result = new ArrayList<>();
        studentRepository.findAll().forEach(s -> result.add(toDto(s)));
        return result;
    }

    public List<StudentDto> getStudentsByCurator(User curator) {
        if (curator.getGroup() == null) {
            return List.of();
        }
        return getStudentsByGroup(curator.getGroup().getId());
    }

    public List<StudentDto> getStudentsByGroup(Long groupId) {
        List<StudentDto> result = new ArrayList<>();
        studentRepository.findByGroupId(groupId).forEach(s -> result.add(toDto(s)));
        return result;
    }

    public Optional<StudentDto> getStudentById(Long id) {
        return studentRepository.findById(id).map(this::toDto);
    }

    public StudentDto createStudent(StudentDto dto, User currentUser) {
        // Curators can only add students to their own group
        if (!currentUser.getRoles().contains("ROLE_ADMIN")) {
            if (currentUser.getGroup() == null) {
                throw new SecurityException("У вас нет прикреплённой группы");
            }
            if (!currentUser.getGroup().getId().equals(dto.getGroupId())) {
                throw new SecurityException("Вы можете добавлять студентов только в свою группу");
            }
        }

        if (studentRepository.existsByRecordBookNumber(dto.getRecordBookNumber())) {
            throw new IllegalArgumentException("Студент с таким номером зачётной книжки уже существует");
        }

        Student student = toEntity(dto);
        Student saved = studentRepository.save(student);
        LOG.info("Student created: {}", saved.getFullName());
        return toDto(saved);
    }

    public StudentDto updateStudent(Long id, StudentDto dto, User currentUser) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден: " + id));

        // Curators can only modify students in their group
        if (!currentUser.getRoles().contains("ROLE_ADMIN")) {
            if (currentUser.getGroup() == null ||
                    !currentUser.getGroup().getId().equals(student.getGroup().getId())) {
                throw new SecurityException("Нет прав для редактирования этого студента");
            }
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
            groupRepository.findById(dto.getGroupId()).ifPresent(student::setGroup);
        }

        Student updated = studentRepository.update(student);
        return toDto(updated);
    }

    public void deleteStudent(Long id, User currentUser) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден: " + id));

        if (!currentUser.getRoles().contains("ROLE_ADMIN")) {
            if (currentUser.getGroup() == null ||
                    !currentUser.getGroup().getId().equals(student.getGroup().getId())) {
                throw new SecurityException("Нет прав для удаления этого студента");
            }
        }

        studentRepository.deleteById(id);
        LOG.info("Student deleted: {}", id);
    }

    private StudentDto toDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setPatronymic(student.getPatronymic());
        dto.setBirthDate(student.getBirthDate());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setEmail(student.getEmail());
        dto.setAddress(student.getAddress());
        dto.setRecordBookNumber(student.getRecordBookNumber());

        if (student.getGroup() != null) {
            dto.setGroupId(student.getGroup().getId());
            dto.setGroupNumber(student.getGroup().getGroupNumber());
        }

        return dto;
    }

    private Student toEntity(StudentDto dto) {
        Student student = new Student();
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setPatronymic(dto.getPatronymic());
        student.setBirthDate(dto.getBirthDate());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setEmail(dto.getEmail());
        student.setAddress(dto.getAddress());
        student.setRecordBookNumber(dto.getRecordBookNumber());

        if (dto.getGroupId() != null) {
            groupRepository.findById(dto.getGroupId()).ifPresent(student::setGroup);
        }

        return student;
    }
}
