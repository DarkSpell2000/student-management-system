package com.studentmanagement.repository;

import com.studentmanagement.model.Group;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupNumber(String groupNumber);

    List<Group> findByFaculty(String faculty);

    List<Group> findByCourse(Integer course);

    boolean existsByGroupNumber(String groupNumber);

    long countByFaculty(String faculty);
}