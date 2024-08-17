package com.example.SpringMate.Service;
import com.example.SpringMate.DTO.UserDTO;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@Slf4j //will create instance for logger
public class UserService {

//    private static final Logger log = LoggerFactory.getLogger(UserService.class); //creating instance of logger
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
                log.error("User already exists with email :: "+userDetails.getEmail());
                res.put("message", "User already exists");
                res.put("created", false);
            }

            return res;
        }catch (Exception e){
            log.error(e.getMessage());
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
