package com.example.SpringMate.Repositoy;

import com.example.SpringMate.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String roleName);
    List<Role> findByNameIn(List<String> names);

}
