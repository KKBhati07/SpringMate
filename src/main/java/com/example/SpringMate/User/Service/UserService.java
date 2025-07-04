package com.example.SpringMate.User.Service;

import com.example.SpringMate.Shared.Service.AwsS3Service;
import com.example.SpringMate.User.DTO.*;
import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.User.Repository.RoleRepository;
import com.example.SpringMate.User.Repository.UserRepository;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.ResponseMapper;
import com.example.SpringMate.Util.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;
    private final AuthHelper authHelper;
    private final RoleRepository roleRepository;
    private final ResponseMapper responseMapper;

    public ResponseEntity<Response<CreateUserResponseDto>> createUser(CreateUserRequestDto userDetails) {
        try {
            Optional<User> user = userRepository.findByEmail(userDetails.getEmail());
            if (user.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response<>(
                        new CreateUserResponseDto(false, true),
                        "User already exists"));
            }

            Optional<Role> role = roleRepository.findByName((userDetails.getRole() == null
                    || userDetails.getRole().isBlank())
                    ? Constants.UserRole.USER
                    : userDetails.getRole().trim().toUpperCase());

            if (role.isEmpty()) {
                throw new RuntimeException("Unable to fetch role");
            }

            User newUser = new User(userDetails, role.get());
            userRepository.save(newUser);
            return ResponseEntity.ok(new Response<CreateUserResponseDto>(
                    new CreateUserResponseDto(true, false),
                    "User created successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<CreateUserResponseDto>(
                            new CreateUserResponseDto(false, false),
                            "Unable to create user"));
        }
    }

    public ResponseEntity<Response<PaginatedResponse<UserDetailsDto>>>
    fetchAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
            Page<User> pagedUsers = userRepository.findAll(pageable);

            PaginatedResponse<UserDetailsDto> paginatedResponse
                    = new PaginatedResponse<>(
                    injectSignedProfileUrl(pagedUsers.getContent()),
                    pagedUsers.getNumber(),
                    pagedUsers.getTotalElements(),
                    pagedUsers.getTotalPages()
            );
            return ResponseEntity.ok(new Response<>(paginatedResponse,"Users fetched successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).
                    body(new Response<>("Internal server error"));
        }

    }

    public ResponseEntity<Response<UserDetailsResponseDto>>
    getUserDetails(UUID uuid, User authenticatedUser) {
        try {
            Optional<User> user = userRepository.findByUuid(uuid);
            if (user.isEmpty() || user.get().isDeleted()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                        .body(new Response<>("User not found"));
            } else {

                UserDetailsDto userDetails = responseMapper.mapUser(user.get());

//                res.put("self", authHelper.compareUserDetails(user.get(), authenticatedUser));
                return ResponseEntity.ok(new Response<UserDetailsResponseDto>(
                        new UserDetailsResponseDto(userDetails,
                                authHelper.compareUserDetails(user.get(),
                                        authenticatedUser)),
                        "User details fetched successfully"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).
                    body(new Response<>("Internal server error"));
        }
    }

    public ResponseEntity<Response<Map<String, Boolean>>>
    deleteUser(UUID uuid, User authenticatedUser) {
        Map<String, Boolean> res = new HashMap<>();
        try {
            User userToDelete;

            if (uuid == null) {
                Optional<User> authUserOpt = userRepository.findByEmail(authenticatedUser.getEmail());
                if (authUserOpt.isEmpty()) {
                    res.put("deleted", false);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new Response<>(res, "Authenticated user not found"));
                }
                userToDelete = authUserOpt.get();
            } else {
                Optional<User> userOpt = userRepository.findByUuid(uuid);
                if (userOpt.isEmpty()) {
                    res.put("deleted", false);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new Response<>(res, "User not found"));
                }
                userToDelete = userOpt.get();
            }

            userToDelete.setDeleted(true);
            userRepository.save(userToDelete);

            res.put("deleted", true);
            return ResponseEntity.ok(new Response<>(res, "User deleted successfully"));

        } catch (Exception e) {
            res.put("deleted", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(res, "Internal server error"));
        }
    }

    public ResponseEntity<Response<Map<String, Boolean>>> restoreUser(UUID uuid) {
        Map<String, Boolean> res = new HashMap<>();
        try {
            Optional<User> userOpt = this.userRepository.findByUuid(uuid);
            if (userOpt.isEmpty()) {
                res.put("restored", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>(res, "User not found"));
            }

            User user = userOpt.get();
            user.setDeleted(false);
            this.userRepository.save(user);
            res.put("restored", true);
            return ResponseEntity.ok(new Response<>(res, "User Restored Successfully"));

        } catch (Exception e) {
            res.put("restored", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(new Response<>(res, "Internal server error"));
        }
    }

    public ResponseEntity<Response<UpdateUserResponseDto>>
    updateUser(UpdateUserRequestDto userDetails) {
        Map<String, Object> res = new HashMap<>();
        try {
            Optional<User> optionalUser = userRepository.findByUuid(userDetails.getUuid());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(
                        HttpStatus.BAD_REQUEST.value()
                ).body(new Response<>(new UpdateUserResponseDto(false),
                        "User not Found"));
            }

            User user = optionalUser.get();
            if (userDetails.getProfileImage() != null) {
                String oldProfilePicUrl = user.getProfileUrl();
                String imageUrl = awsS3Service.uploadImage(Constants.AWS.BUCKET_NAME,
                        AwsS3Directory.PROFILE, userDetails.getProfileImage());
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
                if (existing.isPresent() && !user.getUuid().equals(existing.get().getUuid())) {
                    res.put("updated", false);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new Response<>(new UpdateUserResponseDto(false),
                                    "Email already in use"));
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
            return ResponseEntity.ok(new Response<>(
                    new UpdateUserResponseDto(true, true,
                            responseMapper.mapUser(updatedUser)),
                    "User Updated Successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            res.put("updated", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response<>(null, "Internal server error"));
        }
    }

    private List<UserDetailsDto> injectSignedProfileUrl(List<User> users) {
        return users.stream().map(responseMapper::mapUser).collect(Collectors.toList());
    }

}
