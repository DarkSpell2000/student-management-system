package com.studentmanagement.service;

import com.studentmanagement.dto.GroupDto;
import com.studentmanagement.model.Group;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.UserRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class GroupService {

    private static final Logger LOG = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public List<GroupDto> getAllGroups() {
        List<GroupDto> result = new ArrayList<>();
        groupRepository.findAll().forEach(g -> result.add(toDto(g)));
        return result;
    }

    public Optional<GroupDto> getGroupById(Long id) {
        return groupRepository.findById(id).map(this::toDto);
    }

    public Optional<GroupDto> getGroupByNumber(String groupNumber) {
        return groupRepository.findByGroupNumber(groupNumber).map(this::toDto);
    }

    public GroupDto createGroup(GroupDto dto) {
        if (groupRepository.existsByGroupNumber(dto.getGroupNumber())) {
            throw new IllegalArgumentException("Группа с таким номером уже существует: " + dto.getGroupNumber());
        }

        Group group = new Group();
        group.setGroupNumber(dto.getGroupNumber());
        group.setFaculty(dto.getFaculty());
        group.setCourse(dto.getCourse());

        if (dto.getCuratorId() != null) {
            userRepository.findById(dto.getCuratorId())
                    .ifPresent(group::setCurator);
        }

        Group saved = groupRepository.save(group);
        LOG.info("Group created: {}", saved.getGroupNumber());
        return toDto(saved);
    }

    public GroupDto updateGroup(Long id, GroupDto dto) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + id));

        group.setGroupNumber(dto.getGroupNumber());
        group.setFaculty(dto.getFaculty());
        group.setCourse(dto.getCourse());

        if (dto.getCuratorId() != null) {
            userRepository.findById(dto.getCuratorId())
                    .ifPresent(group::setCurator);
        } else {
            group.setCurator(null);
        }

        Group updated = groupRepository.update(group);
        return toDto(updated);
    }

    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + id));

        if (group.getStudents() != null && !group.getStudents().isEmpty()) {
            throw new IllegalStateException("Нельзя удалить группу, в которой есть студенты");
        }

        groupRepository.deleteById(id);
        LOG.info("Group deleted: {}", id);
    }

    private GroupDto toDto(Group group) {
        GroupDto dto = new GroupDto();
        dto.setId(group.getId());
        dto.setGroupNumber(group.getGroupNumber());
        dto.setFaculty(group.getFaculty());
        dto.setCourse(group.getCourse());
        dto.setStudentCount(group.getStudentCount());

        if (group.getCurator() != null) {
            dto.setCuratorId(group.getCurator().getId());
            dto.setCuratorName(group.getCurator().getFullName());
        }

        return dto;
    }
}
