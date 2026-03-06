package com.studentmanagement.service;

import com.studentmanagement.dto.GroupDto;
import com.studentmanagement.model.Group;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.UserRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

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
        return groupRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<GroupDto> getGroupById(Long id) {
        return groupRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<GroupDto> getGroupByNumber(String groupNumber) {
        return groupRepository.findByGroupNumber(groupNumber)
                .map(this::convertToDto);
    }

    @Transactional
    public GroupDto createGroup(GroupDto groupDto) {
        LOG.info("Creating new group: {}", groupDto.getGroupNumber());

        if (groupRepository.existsByGroupNumber(groupDto.getGroupNumber())) {
            throw new IllegalArgumentException("Группа с таким номером уже существует");
        }

        Group group = new Group();
        group.setGroupNumber(groupDto.getGroupNumber());
        group.setFaculty(groupDto.getFaculty());
        group.setCourse(groupDto.getCourse());
        group.setStudentCount(0);

        if (groupDto.getCuratorId() != null) {
            User curator = userRepository.findById(groupDto.getCuratorId())
                    .orElseThrow(() -> new IllegalArgumentException("Куратор не найден"));
            group.setCurator(curator);
        }

        Group savedGroup = groupRepository.save(group);
        return convertToDto(savedGroup);
    }

    @Transactional
    public GroupDto updateGroup(Long id, GroupDto groupDto) {
        LOG.info("Updating group with id: {}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

        group.setGroupNumber(groupDto.getGroupNumber());
        group.setFaculty(groupDto.getFaculty());
        group.setCourse(groupDto.getCourse());

        if (groupDto.getCuratorId() != null) {
            User curator = userRepository.findById(groupDto.getCuratorId())
                    .orElseThrow(() -> new IllegalArgumentException("Куратор не найден"));
            group.setCurator(curator);
        }

        Group updatedGroup = groupRepository.update(group);
        return convertToDto(updatedGroup);
    }

    @Transactional
    public void deleteGroup(Long id) {
        LOG.info("Deleting group with id: {}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

        if (group.getStudentCount() > 0) {
            throw new IllegalStateException("Нельзя удалить группу, в которой есть студенты");
        }

        groupRepository.delete(group);
    }

    private GroupDto convertToDto(Group group) {
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