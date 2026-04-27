package com.studentmanagement.controller;

import com.studentmanagement.dto.GroupDto;
import com.studentmanagement.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Группы", description = "Управление студенческими группами")
@SecurityRequirement(name = "Bearer Authentication")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    @Operation(summary = "Все группы (admin + curator)")
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Группа по ID")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{groupNumber}")
    @Operation(summary = "Группа по номеру (например: ИТ-21)")
    public ResponseEntity<GroupDto> getGroupByNumber(@PathVariable String groupNumber) {
        return groupService.getGroupByNumber(groupNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать группу [ADMIN]")
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupDto dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить группу [ADMIN]")
    public ResponseEntity<?> updateGroup(@PathVariable Long id, @Valid @RequestBody GroupDto dto) {
        try {
            return ResponseEntity.ok(groupService.updateGroup(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить группу [ADMIN] (только пустые)")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
