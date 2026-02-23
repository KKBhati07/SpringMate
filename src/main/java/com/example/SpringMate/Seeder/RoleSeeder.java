package com.example.SpringMate.Seeder;

import com.example.SpringMate.Shared.Roles;
import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(1)
public class RoleSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        log.info("Role seeding started");

        List<String> roles = List.of(
                Roles.USER,
                Roles.ADMIN,
                Roles.SUPER_ADMIN,
                Roles.PROMETHEUS
        );

        List<String> existingRoles = roleRepository
                .findByNameIn(roles).stream()
                .map(Role::getName).toList();

        List<Role> newRoles = roles.stream()
                .filter(role -> !existingRoles.contains(role))
                .map(role -> new Role(null, role))
                .toList();

        if (!newRoles.isEmpty()) {
            roleRepository.saveAll(newRoles);
            log.info("Role seeding completed addedCount={}", newRoles.size());
        } else {
            log.info("Role seeding skipped no new roles");
        }

    }

}
