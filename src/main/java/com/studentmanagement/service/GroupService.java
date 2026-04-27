package com.studentmanagement.service;

import com.studentmanagement.dto.GroupDto;
import com.studentmanagement.model.Group;
import com.studentmanagement.repository.GroupRepository;
import com.studentmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional   // все методы — транзакционные по умолчанию
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<GroupDto> getGroupById(Long id) {
        return groupRepository.findById(id).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<GroupDto> getGroupByNumber(String groupNumber) {
        return groupRepository.findByGroupNumber(groupNumber).map(this::toDto);
    }

    public GroupDto createGroup(GroupDto dto) {
        if (groupRepository.existsByGroupNumber(dto.getGroupNumber())) {
            throw new IllegalArgumentException(
                "Группа с номером '" + dto.getGroupNumber() + "' уже существует");
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
        log.info("Создана группа: {}", saved.getGroupNumber());
        return toDto(saved);
    }

    public GroupDto updateGroup(Long id, GroupDto dto) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + id));

        // Проверка уникальности номера (исключая текущую)
        if (!group.getGroupNumber().equals(dto.getGroupNumber())
                && groupRepository.existsByGroupNumber(dto.getGroupNumber())) {
            throw new IllegalArgumentException(
                "Группа с номером '" + dto.getGroupNumber() + "' уже существует");
        }

        group.setGroupNumber(dto.getGroupNumber());
        group.setFaculty(dto.getFaculty());
        group.setCourse(dto.getCourse());

        if (dto.getCuratorId() != null) {
            userRepository.findById(dto.getCuratorId())
                    .ifPresent(group::setCurator);
        } else {
            group.setCurator(null);
        }

        return toDto(groupRepository.save(group));
    }

    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + id));

        long studentCount = groupRepository.countStudentsByGroupId(id);
        if (studentCount > 0) {
            throw new IllegalStateException(
                "Нельзя удалить группу: в ней есть " + studentCount + " студент(ов)");
        }

        groupRepository.deleteById(id);
        log.info("Удалена группа id={}", id);
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
