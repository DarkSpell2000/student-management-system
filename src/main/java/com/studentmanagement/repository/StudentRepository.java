package com.studentmanagement.repository;

import com.studentmanagement.model.Group;
import com.studentmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByGroup(Group group);
    List<Student> findByGroupId(Long groupId);
    Optional<Student> findByRecordBookNumber(String recordBookNumber);
    boolean existsByRecordBookNumber(String recordBookNumber);
    boolean existsByRecordBookNumberAndIdNot(String recordBookNumber, Long id);
}
