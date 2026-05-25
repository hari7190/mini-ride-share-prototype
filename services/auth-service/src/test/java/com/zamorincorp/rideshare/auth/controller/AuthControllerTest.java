package com.zamorincorp.rideshare.auth.controller;

import com.zamorincorp.rideshare.auth.entity.User;
import com.zamorincorp.rideshare.auth.repository.UserRepository;
import com.zamorincorp.rideshare.auth.security.JwtProvider;
import com.zamorincorp.rideshare.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final UUID USER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "userService", userService);
    }

    @Test
    void register_delegatesToUserServiceAndReturnsSuccessMessage() {
        User user = sampleUser("alice", "secret");
        doNothing().when(userService).createUser(user);

        String response = authController.register(user);

        verify(userService).createUser(user);
        assertEquals("User registered successfully!", response);
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        User request = sampleUser("alice", "secret");
        User stored = storedUser("alice", "$2a$hashed");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("secret", "$2a$hashed")).thenReturn(true);
        when(jwtProvider.generateToken(USER_ID)).thenReturn("jwt-token");

        ResponseEntity<Map<String, String>> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().get("token"));
    }

    @Test
    void login_withInvalidPassword_returnsUnauthorized() {
        User request = sampleUser("alice", "wrong");
        User stored = storedUser("alice", "$2a$hashed");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        ResponseEntity<Map<String, String>> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("", response.getBody().get("token"));
    }

    @Test
    void login_withUnknownUsername_returnsUnauthorized() {
        User request = sampleUser("nobody", "secret");
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().get("token").isEmpty());
    }

    private User sampleUser(String username, String password) {
        return User.builder()
                .username(username)
                .password(password)
                .role(User.Role.RIDER)
                .build();
    }

    private User storedUser(String username, String hashedPassword) {
        return User.builder()
                .id(USER_ID)
                .username(username)
                .password(hashedPassword)
                .role(User.Role.RIDER)
                .build();
    }

}
