package com.example.SpringMate.Entity;
import com.example.SpringMate.Constants;
import com.example.SpringMate.DTO.UserDTO;
import com.example.SpringMate.Helpers.CoreHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@RequiredArgsConstructor
public class User implements UserDetails {
    public User(UserDTO userDetails){
        this.name = userDetails.getName();
        this.email = userDetails.getEmail();
        this.password = encodePassword(userDetails.getPassword());
        setRoleAndUuid(userDetails.getRole());
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String uuid;
    private String name;
    @JsonIgnore
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    private String role;

    @PrePersist
    public void presets(){
        if(uuid==null){
            this.uuid=CoreHelper.generateUUID();
        }
        if(role==null){
            this.role= Constants.UserRole.USER;
        }

    }
    private void setRoleAndUuid(String role){
        this.uuid= CoreHelper.generateUUID();
        this.role=role != null? role : Constants.UserRole.USER;
    }

    private String encodePassword(String password){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
