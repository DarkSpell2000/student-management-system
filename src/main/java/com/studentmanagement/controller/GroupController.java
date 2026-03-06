package com.studentmanagement.controller;

import com.studentmanagement.dto.GroupDto;
import com.studentmanagement.model.User;
import com.studentmanagement.service.GroupService;
import io.micronaut.http.HttpResponse;
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

@Controller("/api/groups")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "Группы", description = "Управление студенческими группами")
@SecurityRequirement(name = "Bearer Authentication")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Get
    @Operation(summary = "Получить все группы")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список групп",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public List<GroupDto> getAllGroups() {
        return groupService.getAllGroups();
    }

    @Get("/{id}")
    @Operation(summary = "Получить группу по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Группа найдена",
                    content = @Content(schema = @Schema(implementation = GroupDto.class))),
            @ApiResponse(responseCode = "404", description = "Группа не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public HttpResponse<GroupDto> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Get("/number/{groupNumber}")
    @Operation(summary = "Получить группу по номеру")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Группа найдена",
                    content = @Content(schema = @Schema(implementation = GroupDto.class))),
            @ApiResponse(responseCode = "404", description = "Группа не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public HttpResponse<GroupDto> getGroupByNumber(@PathVariable String groupNumber) {
        return groupService.getGroupByNumber(groupNumber)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    @Secured({"ROLE_ADMIN"})
    @Operation(summary = "Создать новую группу", description = "Только для администраторов")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Группа создана",
                    content = @Content(schema = @Schema(implementation = GroupDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Требуются права администратора")
    })
    public HttpResponse<GroupDto> createGroup(@Body @Valid GroupDto groupDto) {
        try {
            GroupDto created = groupService.createGroup(groupDto);
            return HttpResponse.created(created);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest();
        }
    }

    @Put("/{id}")
    @Secured({"ROLE_ADMIN"})
    @Operation(summary = "Обновить группу", description = "Только для администраторов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Группа обновлена",
                    content = @Content(schema = @Schema(implementation = GroupDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Требуются права администратора"),
            @ApiResponse(responseCode = "404", description = "Группа не найдена")
    })
    public HttpResponse<GroupDto> updateGroup(@PathVariable Long id, @Body @Valid GroupDto groupDto) {
        try {
            GroupDto updated = groupService.updateGroup(id, groupDto);
            return HttpResponse.ok(updated);
        } catch (IllegalArgumentException e) {
            return HttpResponse.notFound();
        }
    }

    @Delete("/{id}")
    @Secured({"ROLE_ADMIN"})
    @Operation(summary = "Удалить группу", description = "Только для администраторов")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Группа удалена"),
            @ApiResponse(responseCode = "400", description = "В группе есть студенты"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Требуются права администратора"),
            @ApiResponse(responseCode = "404", description = "Группа не найдена")
    })
    public HttpResponse<?> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return HttpResponse.noContent();
        } catch (IllegalStateException e) {
            return HttpResponse.badRequest(e.getMessage());
        } catch (IllegalArgumentException e) {
            return HttpResponse.notFound();
        }
    }

    @Get("/my")
    @Operation(summary = "Получить группу текущего куратора")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Группа найдена",
                    content = @Content(schema = @Schema(implementation = GroupDto.class))),
            @ApiResponse(responseCode = "404", description = "У куратора нет группы"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public HttpResponse<GroupDto> getMyGroup(Authentication authentication) {
        User user = (User) authentication.getAttributes().get("user");

        if (user.getGroup() == null) {
            return HttpResponse.notFound();
        }

        return groupService.getGroupById(user.getGroup().getId())
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }
}