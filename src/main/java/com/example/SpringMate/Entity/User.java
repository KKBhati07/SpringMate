package com.example.SpringMate.Entity;

import com.example.SpringMate.Util.Constants;
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

@Entity
@Table(name = "users")
@Getter
@Setter
@RequiredArgsConstructor
public class User implements UserDetails {
    public User(UserDTO userDetails, Role role) {
        this.name = userDetails.getName();
        this.email = userDetails.getEmail();
        this.password = encodePassword(userDetails.getPassword());
        this.role = role;
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @Column(name = "profile_url")
    private String profileUrl;
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
    @Column(name = "contact_no", nullable = true)
    private String contactNo;

    @PrePersist
    public void presets() {
        if (uuid == null) {
            this.uuid = CoreHelper.generateUUID();
        }
    }

    private String encodePassword(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.getName()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public boolean isAdmin() {
        return Constants.UserRole.ADMIN.equalsIgnoreCase(this.role.getName());
    }
}
