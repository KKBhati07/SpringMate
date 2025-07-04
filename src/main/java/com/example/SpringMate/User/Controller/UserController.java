package com.example.SpringMate.User.Controller;

import com.example.SpringMate.User.DTO.*;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.User.Service.UserService;
import com.example.SpringMate.Shared.Urls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.User.USER_BASE)
public class UserController {

    private final UserService userService;
    private final AuthHelper authHelper;

    @PostMapping(value = Urls.User.CREATE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<CreateUserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto userDetails) {
        return userService.createUser(userDetails);
    }

    @GetMapping(Urls.User.GET_DETAILS)
    public ResponseEntity<Response<UserDetailsResponseDto>> getUserDetails(@PathVariable UUID uuid, @AuthenticationPrincipal User authenticatedUser) {
        return userService.getUserDetails(uuid, authenticatedUser);
    }

    @DeleteMapping(Urls.User.DELETE_USER)
    public ResponseEntity<Response<Map<String, Boolean>>> deleteUser(@AuthenticationPrincipal User user) {
        return userService.deleteUser(null, user);
    }

    @PutMapping(value = Urls.User.UPDATE_USER,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<UpdateUserResponseDto>> updateUserProfile(@Valid @ModelAttribute UpdateUserRequestDto updatedUserDetails,
                                                                             @AuthenticationPrincipal User autheticatedUser) {
        if (!authHelper.isSelfUUID(updatedUserDetails.getUuid(), autheticatedUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
                    .body(new Response<>(new UpdateUserResponseDto(false), "Cannot update other's profile"));
        }
        return userService.updateUser(updatedUserDetails);
    }
}
