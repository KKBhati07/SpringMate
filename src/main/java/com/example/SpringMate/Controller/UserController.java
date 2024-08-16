package com.example.SpringMate.Controller;

import com.example.SpringMate.DTO.UserDTO;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Response;
import com.example.SpringMate.Service.UserService;
import com.example.SpringMate.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;

@RestController
@RequestMapping(Urls.User.USER_BASE)
public class UserController {

    UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = Urls.User.CREATE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> createUser(@RequestBody UserDTO userDetails){
        System.out.println("Creating user "+ userDetails.toString());
        HashMap<String,Object> result=userService.createUser(userDetails);
        Response response = new Response(result,result.get("message").toString());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
