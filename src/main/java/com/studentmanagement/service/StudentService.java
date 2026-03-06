package com.studentmanagement.service;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.model.Group;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.StudentRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

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
        return studentRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<StudentDto> getStudentsByGroup(Long groupId) {
        return studentRepository.findByGroupId(groupId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<StudentDto> getStudentsByCurator(User curator) {
        if (curator.getGroup() == null) {
            return List.of();
        }
        return getStudentsByGroup(curator.getGroup().getId());
    }

    public Optional<StudentDto> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional
    public StudentDto createStudent(StudentDto studentDto, User curator) {
        LOG.info("Creating new student: {} {}", studentDto.getFirstName(), studentDto.getLastName());

        if (studentRepository.existsByRecordBookNumber(studentDto.getRecordBookNumber())) {
            throw new IllegalArgumentException("Студент с таким номером зачётной книжки уже существует");
        }

        Student student = convertToEntity(studentDto);

        // Assign to group
        if (studentDto.getGroupId() != null) {
            Group group = groupRepository.findById(studentDto.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

            // Check if curator has access to this group
            if (curator.getGroup() != null && !curator.getGroup().getId().equals(group.getId())) {
                throw new SecurityException("Куратор может добавлять студентов только в свою группу");
            }

            group.addStudent(student);
        }

        Student savedStudent = studentRepository.save(student);
        return convertToDto(savedStudent);
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentDto studentDto, User curator) {
        LOG.info("Updating student with id: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

        // Check if curator has access to this student
        if (curator.getGroup() != null &&
                (student.getGroup() == null || !curator.getGroup().getId().equals(student.getGroup().getId()))) {
            throw new SecurityException("Куратор может редактировать только студентов своей группы");
        }

        updateEntityFromDto(student, studentDto);

        Student updatedStudent = studentRepository.update(student);
        return convertToDto(updatedStudent);
    }

    @Transactional
    public void deleteStudent(Long id, User curator) {
        LOG.info("Deleting student with id: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

        // Check if curator has access to this student
        if (curator.getGroup() != null &&
                (student.getGroup() == null || !curator.getGroup().getId().equals(student.getGroup().getId()))) {
            throw new SecurityException("Куратор может удалять только студентов своей группы");
        }

        if (student.getGroup() != null) {
            student.getGroup().decrementStudentCount();
        }

        studentRepository.delete(student);
    }

    private StudentDto convertToDto(Student student) {
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

    private Student convertToEntity(StudentDto dto) {
        Student student = new Student();
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setPatronymic(dto.getPatronymic());
        student.setBirthDate(dto.getBirthDate());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setEmail(dto.getEmail());
        student.setAddress(dto.getAddress());
        student.setRecordBookNumber(dto.getRecordBookNumber());
        return student;
    }

    private void updateEntityFromDto(Student student, StudentDto dto) {
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setPatronymic(dto.getPatronymic());
        student.setBirthDate(dto.getBirthDate());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setEmail(dto.getEmail());
        student.setAddress(dto.getAddress());
        student.setRecordBookNumber(dto.getRecordBookNumber());
    }
}