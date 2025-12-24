package com.example.SpringMate.User.Controller;

import com.example.SpringMate.User.DTO.*;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.User.Exception.UnauthorizedUserUpdateException;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.User.USER_BASE)
public class UserController {

    private final UserService userService;
    private final AuthHelper authHelper;

    @PostMapping(value = Urls.User.CREATE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<CreateUserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto userDetails) {
        return ResponseEntity.ok(new Response<>(userService.createUser(userDetails), "User created successfully"));
    }


    @GetMapping(Urls.User.GET_DETAILS)
    public ResponseEntity<Response<UserDetailsResponseDto>>
    getUserDetails(@PathVariable UUID uuid,
                   @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(new Response<>(userService
                .getUserDetails(uuid, authenticatedUser),
                "User details fetched successfully"));
    }

    @DeleteMapping(Urls.User.DELETE_USER)
    public ResponseEntity<Void>
    deleteUser(@AuthenticationPrincipal User user) {
        log.warn(
                "USER_DELETE requested user=[UUID {}]",
                user.getUuid()
        );

        userService.deleteUser(null, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = Urls.User.UPDATE_USER)
    public ResponseEntity<Response<UpdateUserResponseDto>> updateUserProfile(
            @Valid @RequestBody UpdateUserRequestDto updatedUserDetails,
            @AuthenticationPrincipal User authenticatedUser) {
        if (!authHelper.isSelfUUID(updatedUserDetails.getUuid(), authenticatedUser)) {
            log.warn(
                    "UNAUTHORIZED_USER_UPDATE attempt targetUser={} actor={}",
                    updatedUserDetails.getUuid(),
                    authenticatedUser.getUuid()
            );
            throw new UnauthorizedUserUpdateException();
        }

        return ResponseEntity.ok(new Response<>(userService.updateUser(updatedUserDetails),
                "User updated successfully"));
    }

    @PatchMapping(value = Urls.User.UPLOAD_IMAGE_FALLBACK, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProfileImageFallback(
            @Valid @ModelAttribute FallbackUploadRequestDto dto,
            @AuthenticationPrincipal User authenticatedUser) {

        if (!authHelper.isSelfUUID(dto.getUuid(), authenticatedUser)) {
            log.warn(
                    "UNAUTHORIZED_PROFILE_IMAGE_UPLOAD targetUser={} actor={}",
                    dto.getUuid(),
                    authenticatedUser.getUuid()
            );
            throw new UnauthorizedUserUpdateException();
        }
        userService.uploadProfileImage(dto);

        return ResponseEntity.noContent().build();
    }

}
