package com.example.SpringMate.User.Service;

import com.example.SpringMate.Shared.Service.AwsS3Service;
import com.example.SpringMate.User.DTO.UpdateUserDTO;
import com.example.SpringMate.User.DTO.UserDTO;
import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.User.Repository.RoleRepository;
import com.example.SpringMate.User.Repository.UserRepository;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;
    private final AuthHelper authHelper;
    private final RoleRepository roleRepository;
    private final ResponseMapper responseMapper;

    public Response createUser(UserDTO userDetails) {
        HashMap<String, Object> res = new HashMap<>();
        try {
            Optional<User> user = userRepository.findByEmail(userDetails.getEmail());
            if(user.isPresent()){
                res.put("created", false);
                res.put("already_exists", true);
                return new Response(res, "User already exists");
            }

            Optional<Role> role = roleRepository.findByName((userDetails.getRole() == null
                    || userDetails.getRole().isBlank())
                    ? Constants.UserRole.USER
                    : userDetails.getRole().trim().toUpperCase());

            if(role.isEmpty()){
                throw new RuntimeException("Unable to fetch role");
            }

            User newUser = new User(userDetails, role.get());
            userRepository.save(newUser);
            res.put("created", true);
            return new Response(res, "User created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            res.put("created", false);
            return new Response(res, "Unable to create user");
        }
    }

    public ResponseEntity<Response> fetchAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
            Page<User> pagedUsers = userRepository.findAll(pageable);
            HashMap<String, Object> map = new HashMap<>();
            map.put("users", injectSignedProfileUrl(pagedUsers.getContent()));
            map.put("currentPage", pagedUsers.getNumber());
            map.put("totalItems", pagedUsers.getTotalElements());
            map.put("totalPages", pagedUsers.getTotalPages());
            return ResponseEntity.ok(new Response(map, "Users fetched successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).
                    body(new Response(new HashMap<>(), "Internal server error"));
        }

    }

    public ResponseEntity<Response> getUserDetails(String uuid, User authenticatedUser) {
        try {
            Optional<User> user = userRepository.findByUuid(uuid);
            if (user.isEmpty() || user.get().isDeleted()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                        .body(new Response(new HashMap<>(), "User not found"));
            } else {
                HashMap<String, Object> res = new HashMap<>();
                res.put("user_details", responseMapper.mapUser(user.get()));
                res.put("self", authHelper.compareUserDetails(user.get(), authenticatedUser));
                return ResponseEntity.ok(new Response(res, "User details fetched successfully"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).
                    body(new Response(new HashMap<>(), "Internal server error"));
        }

    }


    public ResponseEntity<Response> deleteUser(String uuid, User authenticatedUser) {
        Map<String, Boolean> res = new HashMap<>();
        try {
            User userToDelete;

            if (uuid == null) {
                Optional<User> authUserOpt = userRepository.findByEmail(authenticatedUser.getEmail());
                if (authUserOpt.isEmpty()) {
                    res.put("deleted", false);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new Response(res, "Authenticated user not found"));
                }
                userToDelete = authUserOpt.get();
            } else {
                Optional<User> userOpt = userRepository.findByUuid(uuid);
                if (userOpt.isEmpty()) {
                    res.put("deleted", false);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new Response(res, "User not found"));
                }
                userToDelete = userOpt.get();
            }

            userToDelete.setDeleted(true);
            userRepository.save(userToDelete);

            res.put("deleted", true);
            return ResponseEntity.ok(new Response(res, "User deleted successfully"));

        } catch (Exception e) {
            res.put("deleted", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(res, "Internal server error"));
        }
    }

    public ResponseEntity<Response> restoreUser(String uuid) {
        Map<String, Boolean> res = new HashMap<>();
        try {
            Optional<User> userOpt = this.userRepository.findByUuid(uuid);
            if (userOpt.isEmpty()) {
                res.put("restored", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response(res, "User not found"));
            }

            User user = userOpt.get();
            user.setDeleted(false);
            this.userRepository.save(user);
            res.put("restored", true);
            return ResponseEntity.ok(new Response(res, "User Restored Successfully"));

        } catch (Exception e) {
            res.put("restored", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(new Response(res, "Internal server error"));
        }
    }

    public ResponseEntity<Response> updateUser(UpdateUserDTO userDetails) {
        Map<String, Object> res = new HashMap<>();
        try {
            Optional<User> optionalUser = userRepository.findByUuid(userDetails.getUuid());
            if(optionalUser.isEmpty()){
                res.put("updated", false);
                return ResponseEntity.status(
                        HttpStatus.BAD_REQUEST.value()
                ).body(new Response(res, "User not Found"));
            }

            User user = optionalUser.get();
            if (userDetails.getProfileImage() != null) {
                String oldProfilePicUrl = user.getProfileUrl();
                String imageUrl = awsS3Service.uploadImage(Constants.AWS.BUCKET_NAME, AwsS3Directory.PROFILE, userDetails.getProfileImage());
                if (imageUrl == null) throw new RuntimeException("Upload Image failed");
                user.setProfileUrl(imageUrl);
                if (oldProfilePicUrl != null) {
                    awsS3Service.deleteImage(Constants.AWS.BUCKET_NAME, oldProfilePicUrl);
                }
            }

            if (userDetails.getName() != null) {
                user.setName(userDetails.getName());
            }
            if (userDetails.getEmail() != null) {
                Optional<User> existing = userRepository.findByEmail(userDetails.getEmail());
                if(existing.isPresent() && !user.getUuid().equals(existing.get().getUuid())){
                    res.put("updated", false);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new Response(res, "Email already in use"));
                }
                user.setEmail(userDetails.getEmail());
            }
            if (userDetails.getContactNo() != null) {
                user.setContactNo(userDetails.getContactNo());
            }
            User updatedUser = this.userRepository.save(user);
            res.put("updated", true);
            res.put("self", true);
            res.put("user_details", responseMapper.mapUser(updatedUser));
            return ResponseEntity.ok(new Response(res, "User Updated Successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            res.put("updated", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(res, "Internal server error"));
        }
    }

    private List<Map<String, Object>> injectSignedProfileUrl(List<User> users) {
        return users.stream().map(responseMapper::mapUser).toList();
    }

}
