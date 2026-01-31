package com.example.SpringMate.User.Controller;

import com.example.SpringMate.User.DTO.*;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.User.Exception.UnauthorizedUserUpdateException;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.User.Service.UserService;
import com.example.SpringMate.Shared.Urls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing user-related operations.
 * Handles profile management and user self-service actions.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.User.BASE)
public class UserController {

    private final UserService userService;
    private final AuthHelper authHelper;

    @PostMapping(value = Urls.User.CREATE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<CreateUserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto userDetails) {
        return ResponseEntity.ok(Response.success(userService.createUser(userDetails), "User created successfully"));
    }


    @GetMapping(Urls.User.GET_DETAILS)
    public ResponseEntity<Response<UserDetailsResponseDto>>
    getUserDetails(@PathVariable UUID uuid,
                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(Response.success(userService
                .getUserDetails(uuid, authenticatedUser),
                "User details fetched successfully"));
    }

    @DeleteMapping(Urls.User.DELETE_USER)
    public ResponseEntity<Void>
    deleteUser(@AuthenticationPrincipal AuthenticatedUser user) {
        log.warn(
                "USER_DELETE requested user=[UUID {}]",
                user.uuid()
        );

        userService.deleteUser(null, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates user profile information.
     * Only allows users to update their own profile.
     */
    @PutMapping(value = Urls.User.UPDATE_USER)
    public ResponseEntity<Response<UpdateUserResponseDto>> updateUserProfile(
            @Valid @RequestBody UpdateUserRequestDto updatedUserDetails,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (!authHelper.isSelfUUID(updatedUserDetails.getUuid(), authenticatedUser.uuid())) {
            log.warn(
                    "UNAUTHORIZED_USER_UPDATE attempt targetUser={} actor={}",
                    updatedUserDetails.getUuid(),
                    authenticatedUser.uuid()
            );
            throw new UnauthorizedUserUpdateException();
        }

        return ResponseEntity.ok(Response.success(userService.updateUser(updatedUserDetails),
                "User updated successfully"));
    }

    /**
     * Uploads profile image using multipart fallback flow.
     * Users are only allowed to upload their own profile image.
     */
    @PatchMapping(value = Urls.User.UPLOAD_IMAGE_FALLBACK, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProfileImageFallback(
            @Valid @ModelAttribute FallbackUploadRequestDto dto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        if (!authHelper.isSelfUUID(dto.getUuid(), authenticatedUser.uuid())) {
            log.warn(
                    "UNAUTHORIZED_PROFILE_IMAGE_UPLOAD targetUser={} actor={}",
                    dto.getUuid(),
                    authenticatedUser.uuid()
            );
            throw new UnauthorizedUserUpdateException();
        }
        userService.uploadProfileImage(dto);

        return ResponseEntity.noContent().build();
    }

}
