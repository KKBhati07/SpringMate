package com.example.SpringMate.DTO;
import com.example.SpringMate.Entity.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

@JsonAutoDetect
@Data
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
