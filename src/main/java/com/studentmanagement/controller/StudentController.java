package com.studentmanagement.controller;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.model.User;
import com.studentmanagement.service.StudentService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.authentication.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@Controller("/api/students")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "Студенты", description = "Управление данными студентов")
@SecurityRequirement(name = "Bearer Authentication")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Get
    @Operation(summary = "Получить всех студентов",
            description = "Возвращает список всех студентов (для администратора) или студентов своей группы (для куратора)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список студентов",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudentDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public List<StudentDto> getAllStudents(Authentication authentication) {
        User user = (User) authentication.getAttributes().get("user");

        if (user.getRoles().contains("ROLE_ADMIN")) {
            return studentService.getAllStudents();
        } else {
            return studentService.getStudentsByCurator(user);
        }
    }

    @Get("/{id}")
    @Operation(summary = "Получить студента по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Студент найден",
                    content = @Content(schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "404", description = "Студент не найден"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public HttpResponse<StudentDto> getStudentById(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getAttributes().get("user");

        return studentService.getStudentById(id)
                .filter(student -> {
                    if (user.getRoles().contains("ROLE_ADMIN")) {
                        return true;
                    }
                    return user.getGroup() != null &&
                            student.getGroupId() != null &&
                            user.getGroup().getId().equals(student.getGroupId());
                })
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    @Operation(summary = "Создать нового студента")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Студент создан",
                    content = @Content(schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав")
    })
    public HttpResponse<StudentDto> createStudent(@Body @Valid StudentDto studentDto, Authentication authentication) {
        User user = (User) authentication.getAttributes().get("user");

        try {
            StudentDto created = studentService.createStudent(studentDto, user);
            return HttpResponse.created(created);
        } catch (SecurityException e) {
            return HttpResponse.status(io.micronaut.http.HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest();
        }
    }

    @Put("/{id}")
    @Operation(summary = "Обновить данные студента")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Студент обновлен",
                    content = @Content(schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    public HttpResponse<StudentDto> updateStudent(@PathVariable Long id, @Body @Valid StudentDto studentDto,
                                                  Authentication authentication) {
        User user = (User) authentication.getAttributes().get("user");

        try {
            StudentDto updated = studentService.updateStudent(id, studentDto, user);
            return HttpResponse.ok(updated);
        } catch (SecurityException e) {
            return HttpResponse.status(io.micronaut.http.HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            return HttpResponse.notFound();
        }
    }

    @Delete("/{id}")
    @Operation(summary = "Удалить студента")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Студент удален"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    public HttpResponse<?> deleteStudent(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getAttributes().get("user");

        try {
            studentService.deleteStudent(id, user);
            return HttpResponse.noContent();
        } catch (SecurityException e) {
            return HttpResponse.status(io.micronaut.http.HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            return HttpResponse.notFound();
        }
    }

    @Get("/group/{groupId}")
    @Operation(summary = "Получить студентов по группе")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список студентов группы",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudentDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав")
    })
    public HttpResponse<List<StudentDto>> getStudentsByGroup(@PathVariable Long groupId, Authentication authentication) {
        User user = (User) authentication.getAttributes().get("user");

        if (!user.getRoles().contains("ROLE_ADMIN") &&
                (user.getGroup() == null || !user.getGroup().getId().equals(groupId))) {
            return HttpResponse.status(io.micronaut.http.HttpStatus.FORBIDDEN).body(null);
        }

        return HttpResponse.ok(studentService.getStudentsByGroup(groupId));
    }
}