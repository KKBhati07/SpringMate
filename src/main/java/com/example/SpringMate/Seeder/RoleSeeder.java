package com.example.SpringMate.Seeder;

import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Arrays;

@Component
public class RoleSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> roles = Arrays.asList("USER", "ADMIN");

        List<String> existingRoles = roleRepository
                .findByNameIn(roles).stream()
                .map(Role::getName).toList();

        List<Role> newRoles = roles.stream()
                .filter(role->!existingRoles.contains(role))
                .map(role->new Role(null,role))
                .toList();

        if(!newRoles.isEmpty()){
            roleRepository.saveAll(newRoles);
        }

    }

}
