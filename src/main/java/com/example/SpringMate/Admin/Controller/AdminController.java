package com.example.SpringMate.Admin.Controller;

import com.example.SpringMate.User.DTO.UpdateUserRequestDto;
import com.example.SpringMate.User.DTO.UpdateUserResponseDto;
import com.example.SpringMate.Util.UserDetailsDto;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.User.Service.UserService;
import com.example.SpringMate.Shared.Urls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Admin.ADMIN_BASE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping(value = Urls.Admin.User.FETCH_ALL)
    public ResponseEntity<Response<PaginatedResponse<UserDetailsDto>>> fetchAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                new Response<>(userService.fetchAll(page, size),
                "Users fetched successfully")
        );
    }

    @DeleteMapping(value = Urls.Admin.User.DELETE)
    public ResponseEntity<Response<Map<String,Boolean>>> deleteUser(@PathVariable UUID uuid) {
        return ResponseEntity.ok(new Response<>(userService.deleteUser(uuid, null),"User deleted successfully"));
    }

    @PatchMapping(value = Urls.Admin.User.RESTORE)
    public ResponseEntity<Response<Map<String,Boolean>>> restoreUser(@PathVariable UUID uuid) {
        return ResponseEntity.ok(new Response<>(userService.restoreUser(uuid),"User restored successfully"));
    }

    @PutMapping(
            value = Urls.Admin.User.UPDATE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<UpdateUserResponseDto>> updateUser(@ModelAttribute UpdateUserRequestDto updatedUser) {
        return ResponseEntity.ok(new Response<>(userService.updateUser(updatedUser),
                "User updated successfully"));
    }
}
