package com.example.SpringMate.Service;
import com.example.SpringMate.DTO.UserDTO;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public HashMap<String,Object> createUser(UserDTO userDetails){
        HashMap<String,Object> res = new HashMap<>();
        try{
            User user = userRepository.findByEmail(userDetails.getEmail());
            if(user == null){
                User newUser = new User(userDetails);
                userRepository.save(newUser);
                res.put("message", "User created successfully");
                res.put("created", true);
            }else {
                res.put("message", "User already exists");
                res.put("created", false);
            }

            return res;
        }catch (Exception e){
            System.out.println(">>>>>"+e.toString());
            System.out.println(">>>>>   "+e.getMessage());
            res.put("message", "Unable to create user");
            res.put("created", false);
            return res;
        }
    }

    public HashMap<String,List<User>> fetchAll(){
        HashMap<String,List<User>> map = new HashMap<>();
        map.put("users", userRepository.findAll());
        return map;

    }


}
