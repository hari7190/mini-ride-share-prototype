package com.zamorincorp.rideshare.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zamorincorp.rideshare.auth.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
