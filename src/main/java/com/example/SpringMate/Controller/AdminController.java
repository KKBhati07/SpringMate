package com.example.SpringMate.Controller;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Service.UserService;
import com.example.SpringMate.Util.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Response> fetchAll(){
        return ResponseEntity.ok(userService.fetchAll());
    }
}
