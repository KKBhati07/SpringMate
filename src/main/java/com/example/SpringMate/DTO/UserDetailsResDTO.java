package com.example.SpringMate.DTO;
import com.example.SpringMate.Entity.User;

public class UserDetailsResDTO {
    private String uuid;
    private String name;
    private String email;
    private String profileUrl;

    public UserDetailsResDTO(User user){
        this.uuid = user.getUuid();
        this.name = user.getName();
        this.email = user.getEmail();
        this.profileUrl = user.getProfileUrl();
    }
}
