package com.example.SpringMate.User.Service;

import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Service.AwsS3Service;
import com.example.SpringMate.User.DTO.*;
import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.User.Exception.EmailAlreadyInUseException;
import com.example.SpringMate.User.Exception.UserAlreadyExistsException;
import com.example.SpringMate.User.Exception.UserNotFoundException;
import com.example.SpringMate.User.Repository.RoleRepository;
import com.example.SpringMate.User.Repository.UserRepository;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.ResponseMapper;
import com.example.SpringMate.Util.UserDetailsDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ListingService listingService;

    public CreateUserResponseDto createUser(CreateUserRequestDto userDetails) {
        Optional<User> user = userRepository.findByEmail(userDetails.getEmail());
        if (user.isPresent()) {
            throw new UserAlreadyExistsException();
        }

        Optional<Role> role = roleRepository.findByName(
                (userDetails.getRole() == null || userDetails.getRole().isBlank())
                        ? Constants.UserRole.USER
                        : userDetails.getRole().trim().toUpperCase()
        );

        if (role.isEmpty()) {
            throw new RuntimeException("Unable to fetch role");
        }

        User newUser = new User(userDetails, role.get());
        userRepository.save(newUser);

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
                   User authenticatedUser) {
//        try {
        Optional<User> user = userRepository.findByUuid(uuid);
        if (user.isEmpty() || user.get().isDeleted()) {
            throw new UserNotFoundException();
        } else {

            UserDetailsDto userDetails = responseMapper.mapUser(user.get());
            return new UserDetailsResponseDto(userDetails,
                    authHelper.compareUserDetails(user.get(),
                            authenticatedUser));
        }
    }

    @Transactional
    public Map<String, Boolean>
    deleteUser(UUID uuid, User authenticatedUser) {
        Map<String, Boolean> res = new HashMap<>();
        Long userId = null;

        if (uuid == null) {
            Optional<User> authUserOpt = userRepository.findByEmail(authenticatedUser.getEmail());
            if (authUserOpt.isEmpty()) {
                res.put("deleted", false);
                throw new UserNotFoundException("Authenticated user not found");
            }
            userId = authUserOpt.get().getId();
        } else {
            Optional<User> userOpt = userRepository.findByUuidAndDeletedFalse(uuid);
            if (userOpt.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }
            userId = userOpt.get().getId();
        }

        userRepository.softDeleteByUuid(uuid != null ? uuid : authenticatedUser.getUuid());
        listingService.softDeleteByUserId(userId);


        res.put("deleted", true);
        return res;

    }

    public Map<String, Boolean> restoreUser(UUID uuid) {
        Map<String, Boolean> res = new HashMap<>();
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(UserNotFoundException::new);

        user.setDeleted(false);
        userRepository.save(user);
        res.put("restored", true);
        return res;
    }

    public UpdateUserResponseDto
    updateUser(UpdateUserRequestDto userDetails) {
        Optional<User> optionalUser = userRepository.findByUuid(userDetails.getUuid());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException();
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
                throw new EmailAlreadyInUseException();
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getContactNo() != null) {
            user.setContactNo(userDetails.getContactNo());
        }
        User updatedUser = this.userRepository.save(user);
        return
                new UpdateUserResponseDto(true, true,
                        responseMapper.mapUser(updatedUser));
    }

    private List<UserDetailsDto> injectSignedProfileUrl(List<User> users) {
        return users.stream().map(responseMapper::mapUser).collect(Collectors.toList());
    }

}
