package com.studentmanagement.repository;

import com.studentmanagement.model.Group;
import com.studentmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupNumber(String groupNumber);
    Optional<Group> findByCurator(User curator);
    boolean existsByGroupNumber(String groupNumber);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.group.id = :groupId")
    long countStudentsByGroupId(Long groupId);
}
