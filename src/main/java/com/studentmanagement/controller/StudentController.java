package com.studentmanagement.controller;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Студенты", description = "Управление данными студентов")
@SecurityRequirement(name = "Bearer Authentication")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Список студентов",
        description = "Администратор: все студенты. Куратор: только своя группа")
    public ResponseEntity<List<StudentDto>> getStudents(Authentication auth) {
        return ResponseEntity.ok(studentService.getStudentsForUser(auth.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Студент по ID (с проверкой прав)")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id, Authentication auth) {
        return studentService.getStudentById(id, auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Студенты по группе (куратор — только свою)")
    public ResponseEntity<?> getStudentsByGroup(@PathVariable Long groupId, Authentication auth) {
        try {
            return ResponseEntity.ok(studentService.getStudentsByGroup(groupId, auth.getName()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "Добавить студента",
        description = "Администратор: в любую группу. Куратор: только в свою группу")
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentDto dto, Authentication auth) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(studentService.createStudent(dto, auth.getName()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить студента (с проверкой прав)")
    public ResponseEntity<?> updateStudent(@PathVariable Long id,
                                           @Valid @RequestBody StudentDto dto,
                                           Authentication auth) {
        try {
            return ResponseEntity.ok(studentService.updateStudent(id, dto, auth.getName()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить студента (с проверкой прав)")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id, Authentication auth) {
        try {
            studentService.deleteStudent(id, auth.getName());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
