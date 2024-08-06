package com.example.SpringMate.Repositoy;
import com.example.SpringMate.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUuid(String uuid);
    User findByEmail(String email);
}
