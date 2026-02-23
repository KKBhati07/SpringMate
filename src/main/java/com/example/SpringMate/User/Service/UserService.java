package com.example.SpringMate.User.Service;

import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Exception.InternalServerException;
import com.example.SpringMate.Shared.Helper.InputSanitizer;
import com.example.SpringMate.Shared.Roles;
import com.example.SpringMate.Storage.Service.StorageService;
import com.example.SpringMate.User.DTO.*;
import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.User.Exception.EmailAlreadyInUseException;
import com.example.SpringMate.User.Exception.UserAlreadyExistsException;
import com.example.SpringMate.User.Exception.UserNotFoundException;
import com.example.SpringMate.User.Repository.RoleRepository;
import com.example.SpringMate.User.Repository.UserRepository;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.ResponseMapper;
import com.example.SpringMate.Util.UserDetailsDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthHelper authHelper;
    private final ResponseMapper responseMapper;
    private final ListingService listingService;
    private final CoreUserService coreUserService;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;

    public CreateUserResponseDto createUser(CreateUserRequestDto userDetails) {
        Optional<User> user = userRepository.findByEmail(userDetails.getEmail());
        if (user.isPresent()) {
            throw new UserAlreadyExistsException();
        }

        Optional<Role> role = roleRepository.findByName(
                (userDetails.getRole() == null || userDetails.getRole().isBlank())
                        ? Roles.USER
                        : userDetails.getRole().trim().toUpperCase()
        );

        if (role.isEmpty()) {
            log.error("Error fetching roles");
            throw new InternalServerException("Unable to fetch role");
        }

        User newUser = User.builder()
                .name(InputSanitizer.sanitizePlainText(userDetails.getName()))
                .email(userDetails.getEmail())
                .password(passwordEncoder.encode(userDetails.getPassword()))
                .role(role.get())
                .build();
        userRepository.save(newUser);
        log.info(
                "User created user=[UUID {}] role={}",
                newUser.getUuid(),
                role.get().getName()
        );


        return new CreateUserResponseDto(true, false);
    }


    public PaginatedResponse<UserDetailsDto> fetchAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<User> pagedUsers = userRepository.findAll(pageable);


        return new PaginatedResponse<>(
                injectSignedProfileUrl(pagedUsers.getContent()),
                pagedUsers.getNumber(),
                pagedUsers.getTotalElements(),
                pagedUsers.getTotalPages()
        );
    }

    public UserDetailsResponseDto
    getUserDetails(UUID uuid,
                   AuthenticatedUser authenticatedUser) {
        User user = coreUserService.getUserOrThrowByUUID(uuid);
        UserDetailsDto userDetails = responseMapper.mapUser(user);
        return new UserDetailsResponseDto(userDetails,
                authHelper.isSelfUUID(user.getUuid(),
                        authenticatedUser.uuid()));

    }

    /**
     * Cascades soft deletion to user's listings to maintain data consistency.
     */
    @Transactional
    public void
    deleteUser(UUID uuid, AuthenticatedUser authenticatedUser) {
        Long userId = null;

        if (uuid == null) {
            userId = coreUserService.getUserOrThrowByEmail(authenticatedUser.email()).getId();
        } else {
            Optional<User> userOpt = userRepository.findByUuidAndDeletedFalse(uuid);
            if (userOpt.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }
            userId = userOpt.get().getId();
        }

        UUID toDeleteUuid = uuid != null ? uuid : authenticatedUser.uuid();
        userRepository.softDeleteByUuid(toDeleteUuid);
        log.warn(
                "User deleted targetUser=[UUID {}] deletedBy=[UUID {}]",
                toDeleteUuid,
                authenticatedUser.uuid()
        );
        listingService.softDeleteByUserId(userId);

    }

    public void restoreUser(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(UserNotFoundException::new);

        user.setDeleted(false);
        log.info(
                "Restored user=[UUID {}]",
                uuid
        );
        userRepository.save(user);
    }

    public UpdateUserResponseDto
    updateUser(UpdateUserRequestDto userDetails) {

        User user = coreUserService.getUserOrThrowByUUID(userDetails.getUuid());

        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getProfileUrl() != null) {
            String oldProfilePicUrl = user.getProfileUrl();
            if (oldProfilePicUrl != null) {
                storageService.deleteImage(oldProfilePicUrl);
            }
            user.setProfileUrl(userDetails.getProfileUrl());
        }
        if (userDetails.getEmail() != null) {
            Optional<User> existing = userRepository.findByEmail(userDetails.getEmail());
            if (existing.isPresent() && !user.getUuid().equals(existing.get().getUuid())) {
                throw new EmailAlreadyInUseException();
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getContactNo() != null) {
            user.setContactNo(userDetails.getContactNo());
        }
        User updatedUser = this.userRepository.save(user);
        log.warn(
                "Updated user=[UUID {}]",
                updatedUser.getUuid()
        );
        return
                new UpdateUserResponseDto(true, true,
                        responseMapper.mapUser(updatedUser));
    }

    public void uploadProfileImage(FallbackUploadRequestDto dto) {

        User user = coreUserService.getUserOrThrowByUUID(dto.getUuid());

        MultipartFile file = dto.getFile();
        if (file != null && !file.isEmpty()) {

            String oldProfilePicUrl = user.getProfileUrl();

            String imageKey = storageService.uploadImage(AwsS3Directory.PROFILE, file);

            if (imageKey == null) {
                throw new InternalServerException("Upload image failed");
            }

            user.setProfileUrl(imageKey);

            if (oldProfilePicUrl != null) {
                storageService.deleteImage(oldProfilePicUrl);
            }
            log.info(
                    "Profile image updated for user=[UUID {}]",
                    user.getUuid()
            );

            userRepository.save(user);
        }
    }


    private List<UserDetailsDto> injectSignedProfileUrl(List<User> users) {
        return users.stream().map(responseMapper::mapUser).collect(Collectors.toList());
    }

}
