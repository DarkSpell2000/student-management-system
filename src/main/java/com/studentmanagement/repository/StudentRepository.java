package com.studentmanagement.repository;

import com.studentmanagement.model.Group;
import com.studentmanagement.model.Student;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {

    List<Student> findByGroup(Group group);

    List<Student> findByGroupId(Long groupId);

    Optional<Student> findByRecordBookNumber(String recordBookNumber);

    boolean existsByRecordBookNumber(String recordBookNumber);
}
