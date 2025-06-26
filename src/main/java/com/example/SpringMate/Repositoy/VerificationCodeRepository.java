package com.example.SpringMate.Repositoy;

import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    void deleteByUserAndType(User user, String type);
    Optional<VerificationCode> findTopByUserAndTypeOrderByCreatedAtDesc(User user, String type);


}
