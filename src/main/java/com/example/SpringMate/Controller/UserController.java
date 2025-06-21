package com.example.SpringMate.Controller;

import com.example.SpringMate.DTO.UpdateUserDTO;
import com.example.SpringMate.DTO.UserDTO;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Helpers.AuthHelper;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Service.UserService;
import com.example.SpringMate.Util.Urls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.User.USER_BASE)
public class UserController {

    private final UserService userService;
    private final AuthHelper authHelper;

    @PostMapping(value = Urls.User.CREATE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> createUser(@RequestBody UserDTO userDetails) {
        return ResponseEntity.ok(userService.createUser(userDetails));
    }

    @GetMapping(Urls.User.GET_DETAILS)
    public ResponseEntity<Response> getUserDetails(@PathVariable String uuid,
                                                   @AuthenticationPrincipal User authenticatedUser) {
        return userService.getUserDetails(uuid, authenticatedUser);
    }

    @DeleteMapping(Urls.User.DELETE_USER)
    public ResponseEntity<Response> deleteUser(@AuthenticationPrincipal User user) {
        return userService.deleteUser(null, user);
    }

    @PutMapping(value = Urls.User.UPDATE_USER,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserProfile(@ModelAttribute UpdateUserDTO updatedUserDetails,
                                               @AuthenticationPrincipal User autheticatedUser) {
        if(!authHelper.isSelfUUID(updatedUserDetails.getUuid(), autheticatedUser)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
                    .body(new Response(new HashMap<>(), "Cannot update other's profile"));
        }
        return userService.updateUser(updatedUserDetails);
    }
}
