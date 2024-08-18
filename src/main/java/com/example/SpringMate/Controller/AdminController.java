package com.example.SpringMate.Controller;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Response;
import com.example.SpringMate.Service.ExternalApiService;
import com.example.SpringMate.Service.UserService;
import com.example.SpringMate.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(Urls.Admin.ADMIN_BASE)
public class AdminController {

    private final UserService userService;
    private final ExternalApiService externalApiService;

    @Autowired
    public AdminController(UserService userService, ExternalApiService externalApiService) {
        this.userService = userService;
        this.externalApiService = externalApiService;
    }

    @GetMapping(value = Urls.Admin.User.FETCH_ALL)
    public ResponseEntity<Response> fetchAll(){
        HashMap<String, List<User>> users = userService.fetchAll();
        Response response = new Response(users,"Users fetched successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/get/{with_exchange}")
    public ResponseEntity<Response> externalApiResponse(@PathVariable("with_exchange") boolean with_exchange){
        HashMap<String, Object> map = new HashMap<>();
        String message = "";
        if(with_exchange){
            map.put("external_api_data",externalApiService.getDataWithExchange());
            message = "External API data fetched successfully with exchange";
        }else {
            map.put("external_api_data",externalApiService.getData());
            message = "External API data fetched successfully";
        }
        Response response = new Response(map,message);
        return ResponseEntity.ok(response);
    }
}
