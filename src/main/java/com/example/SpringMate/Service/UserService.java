package com.example.SpringMate.Service;
import com.example.SpringMate.DTO.UserDTO;
import com.example.SpringMate.DTO.UserDetailsResDTO;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Helpers.AuthHelper;
import com.example.SpringMate.Repositoy.UserRepository;
import com.example.SpringMate.Util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Response createUser(UserDTO userDetails) {
        HashMap<String, Object> res = new HashMap<>();
        try {
            Optional<User> user = userRepository.findByEmail(userDetails.getEmail());
            if (user.isEmpty()) {
                User newUser = new User(userDetails);
                userRepository.save(newUser);
                res.put("created", true);
                return new Response(res, "User created successfully");
            } else {
                res.put("created", false);
                res.put("already_exists", true);
                return new Response(res, "User already exists");
            }

        } catch (Exception e) {
            e.printStackTrace();
            res.put("created", false);
            return new Response(res, "Unable to create user");
        }
    }

    public ResponseEntity<Response> fetchAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> pagedUsers = userRepository.findAll(pageable);
            HashMap<String, Object> map = new HashMap<>();
            map.put("users", pagedUsers.getContent());
            map.put("currentPage", pagedUsers.getNumber());
            map.put("totalItems", pagedUsers.getTotalElements());
            map.put("totalPages", pagedUsers.getTotalPages());
            return ResponseEntity.ok(new Response(map, "Users fetched successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(new HashMap<>(), "Internal server error"));
        }

    }

    public ResponseEntity<Response> getUserDetails(String uuid) {
        try {
            Optional<User> user = userRepository.findByUuid(uuid);
            if (user.isEmpty() || user.get().isDeleted()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response(new HashMap<>(), "User not found"));
            } else {
                HashMap<String, Object> res = new HashMap<>();
                res.put("user_details", new UserDetailsResDTO(user.get()));
                res.put("self", new AuthHelper().compareUserDetails(user.get()));
                return ResponseEntity.ok(new Response(res, "User details fetched successfully"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(new HashMap<>(), "Internal server error"));
        }

    }


    public ResponseEntity<Response> deleteUser(String uuid) {
        Map<String, Boolean> res = new HashMap<>();
        try {
            if (uuid == null) {
                User user = this.userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get();
                user.setDeleted(true);
                this.userRepository.save(user);
                res.put("deleted", true);
                return ResponseEntity.ok(new Response(res, "User Deleted Successfully"));
            }
            if (!this.userRepository.existsByUuid(uuid)) {
                res.put("deleted", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(res, "User not Found"));

            }
            User user = this.userRepository.findByUuid(uuid).get();
            user.setDeleted(true);
            this.userRepository.save(user);
            res.put("deleted", true);
            return ResponseEntity.ok(new Response(res, "User Deleted Successfully"));

        } catch (Exception e) {
            res.put("deleted", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(res, "Internal server error"));

        }
    }

    public ResponseEntity<Response> restoreUser(String uuid) {
        Map<String, Boolean> res = new HashMap<>();
        try {
            if (!this.userRepository.existsByUuid(uuid)) {
                res.put("restored", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(res, "User not Found"));
            }
            User user = this.userRepository.findByUuid(uuid).get();
            user.setDeleted(false);
            this.userRepository.save(user);
            res.put("restored", true);
            return ResponseEntity.ok(new Response(res, "User Restored Successfully"));

        } catch (Exception e) {
            res.put("restored", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(res, "Internal server error"));
        }
    }

}
