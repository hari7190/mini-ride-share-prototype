package com.zamorincorp.rideshare.auth.controller;

import com.zamorincorp.rideshare.auth.entity.User;
import com.zamorincorp.rideshare.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // Hash the password before saving!
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully!";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        // We will implement JWT logic here next
        return "Coming soon: JWT Token";
    }
}
