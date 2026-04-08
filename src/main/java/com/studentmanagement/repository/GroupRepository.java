package com.studentmanagement.repository;

import com.studentmanagement.model.Group;
import com.studentmanagement.model.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long> {

    Optional<Group> findByGroupNumber(String groupNumber);

    Optional<Group> findByCurator(User curator);

    boolean existsByGroupNumber(String groupNumber);
}
