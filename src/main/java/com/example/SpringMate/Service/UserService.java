package com.example.SpringMate.Service;
import com.example.SpringMate.DTO.UserDTO;
import com.example.SpringMate.DTO.UserDetailsResDTO;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Helpers.AuthHelper;
import com.example.SpringMate.Repositoy.UserRepository;
import com.example.SpringMate.Util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Response createUser(UserDTO userDetails){
        HashMap<String,Object> res = new HashMap<>();
        try{
            User user = userRepository.findByEmail(userDetails.getEmail());
            if(user == null){
                User newUser = new User(userDetails);
                userRepository.save(newUser);
                res.put("created", true);
                return new Response(res,"User created successfully");
            }else {
                res.put("created", false);
                res.put("already_exists", true);
                return new Response(res,"User already exists");
            }

        }catch (Exception e){
            e.printStackTrace();
            res.put("created", false);
            return new Response(res,"Unable to create user");
        }
    }

    public Response fetchAll(){
        HashMap<String,List<User>> map = new HashMap<>();
        map.put("users", userRepository.findAll());
        return new Response(map,"Users fetched successfully");
    }

    public ResponseEntity<Response> getUserDetails(String uuid){
        User user = userRepository.findByUuid(uuid);
        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(new HashMap<>(),"User not found"));
        }else{
            HashMap<String,Object> res = new HashMap<>();
            res.put("user_details", new UserDetailsResDTO(user));
            res.put("self",new AuthHelper().compareUserDetails(user));
            return ResponseEntity.ok(new Response(res,"User details fetched successfully"));
        }
    }


}
