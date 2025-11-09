package com.example.SpringMate.User.Service;

import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.User.Exception.UserNotFoundException;
import com.example.SpringMate.User.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoreUserService {
    private final UserRepository userRepository;

    public User getUserOrThrowById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public User getUserOrThrowByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElse(null);
    }

    public User getUserOrThrowByUUID(UUID uuid) {
        return userRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(UserNotFoundException::new);
    }

}
