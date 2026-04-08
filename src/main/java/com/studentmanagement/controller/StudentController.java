package com.studentmanagement.controller;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.service.StudentService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
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
    private final UserRepository userRepository;

    public StudentController(StudentService studentService, UserRepository userRepository) {
        this.studentService = studentService;
        this.userRepository = userRepository;
    }

    private User resolveUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    @Get
    @Operation(summary = "Получить всех студентов",
            description = "Администратор видит всех; куратор — только студентов своей группы")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список студентов",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudentDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public List<StudentDto> getAllStudents(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user.getRoles().contains("ROLE_ADMIN")) {
            return studentService.getAllStudents();
        }
        return studentService.getStudentsByCurator(user);
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
        User user = resolveUser(authentication);

        return studentService.getStudentById(id)
                .filter(dto -> {
                    if (user.getRoles().contains("ROLE_ADMIN")) return true;
                    return user.getGroup() != null &&
                            dto.getGroupId() != null &&
                            user.getGroup().getId().equals(dto.getGroupId());
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
            @ApiResponse(responseCode = "403", description = "Нет прав")
    })
    public HttpResponse<StudentDto> createStudent(@Body @Valid StudentDto studentDto, Authentication authentication) {
        User user = resolveUser(authentication);
        try {
            return HttpResponse.created(studentService.createStudent(studentDto, user));
        } catch (SecurityException e) {
            return HttpResponse.status(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest();
        }
    }

    @Put("/{id}")
    @Operation(summary = "Обновить данные студента")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Студент обновлён",
                    content = @Content(schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    public HttpResponse<StudentDto> updateStudent(@PathVariable Long id,
                                                   @Body @Valid StudentDto studentDto,
                                                   Authentication authentication) {
        User user = resolveUser(authentication);
        try {
            return HttpResponse.ok(studentService.updateStudent(id, studentDto, user));
        } catch (SecurityException e) {
            return HttpResponse.status(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return HttpResponse.notFound();
        }
    }

    @Delete("/{id}")
    @Operation(summary = "Удалить студента")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Студент удалён"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    public HttpResponse<?> deleteStudent(@PathVariable Long id, Authentication authentication) {
        User user = resolveUser(authentication);
        try {
            studentService.deleteStudent(id, user);
            return HttpResponse.noContent();
        } catch (SecurityException e) {
            return HttpResponse.status(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return HttpResponse.notFound();
        }
    }

    @Get("/group/{groupId}")
    @Operation(summary = "Получить студентов группы")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список студентов",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudentDto.class)))),
            @ApiResponse(responseCode = "403", description = "Нет прав")
    })
    public HttpResponse<List<StudentDto>> getStudentsByGroup(@PathVariable Long groupId,
                                                              Authentication authentication) {
        User user = resolveUser(authentication);
        if (!user.getRoles().contains("ROLE_ADMIN") &&
                (user.getGroup() == null || !user.getGroup().getId().equals(groupId))) {
            return HttpResponse.status(HttpStatus.FORBIDDEN);
        }
        return HttpResponse.ok(studentService.getStudentsByGroup(groupId));
    }
}
