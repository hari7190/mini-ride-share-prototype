package com.zamorincorp.rideshare.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.zamorincorp.rideshare.auth.entity.User;
import com.zamorincorp.rideshare.auth.repository.UserRepository;
import com.zamorincorp.rideshare.auth.producer.UserCreatedKafkaProducer;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.zamorincorp.rideshare.auth.dto.UserCreatedEvent;
import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCreatedKafkaProducer userCreatedKafkaProducer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUser(User user) {
        // Hash the password before saving!
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent(
            user.getId().toString(),
            user.getUsername(),
            user.getRole().name(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        userCreatedKafkaProducer.sendUserCreatedEvent(userCreatedEvent);
    }
}
