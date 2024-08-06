package com.example.SpringMate.Controller;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Response;
import com.example.SpringMate.Service.UserService;
import com.example.SpringMate.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(Urls.Admin.ADMIN_BASE)
public class AdminController {

    private final UserService userService;
    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = Urls.Admin.User.FETCH_ALL)
    public ResponseEntity<Response> fetchAll(){
        HashMap<String, List<User>> users = userService.fetchAll();
        Response response = new Response(users,"Users fetched successfully");
        return ResponseEntity.ok(response);
    }
}
