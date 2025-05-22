package com.example.SpringMate.Controller;

import com.example.SpringMate.DTO.UpdateUserDTO;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Service.UserService;
import com.example.SpringMate.Util.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Urls.Admin.ADMIN_BASE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = Urls.Admin.User.FETCH_ALL)
    public ResponseEntity<Response> fetchAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.fetchAll(page, size);
    }

    @DeleteMapping(value = Urls.Admin.User.DELETE)
    public ResponseEntity<Response> deleteUser(@PathVariable String uuid) {
        return this.userService.deleteUser(uuid);
    }

    @PatchMapping(value = Urls.Admin.User.RESTORE)
    public ResponseEntity<Response> restoreUser(@PathVariable String uuid) {
        return this.userService.restoreUser(uuid);
    }

    @PutMapping(
            value = Urls.Admin.User.UPDATE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> updateUser(@ModelAttribute UpdateUserDTO updatedUser) {
        return this.userService.updateUser(updatedUser);
    }
}
