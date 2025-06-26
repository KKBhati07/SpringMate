package com.example.SpringMate.Auth.Repository;

import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Admin.Entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    void deleteByUserAndType(User user, String type);
    Optional<VerificationCode> findTopByUserAndTypeOrderByCreatedAtDesc(User user, String type);


}
