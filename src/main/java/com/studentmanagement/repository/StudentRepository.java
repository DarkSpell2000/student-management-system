package com.studentmanagement.repository;

import com.studentmanagement.model.Student;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByGroupId(Long groupId);

    List<Student> findByLastNameContainingIgnoreCase(String lastName);

    Optional<Student> findByRecordBookNumber(String recordBookNumber);

    boolean existsByRecordBookNumber(String recordBookNumber);

    long countByGroupId(Long groupId);
}