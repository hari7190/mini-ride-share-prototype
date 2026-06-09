package foo.hari.rideshare.auth.service;

import foo.hari.rideshare.auth.dto.UserCreatedEvent;
import foo.hari.rideshare.auth.entity.User;
import foo.hari.rideshare.auth.producer.UserCreatedKafkaProducer;
import foo.hari.rideshare.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UUID USER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCreatedKafkaProducer userCreatedKafkaProducer;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<UserCreatedEvent> eventCaptor;

    @Test
    void createUser_hashesPasswordSavesUserAndPublishesEvent() {
        User incoming = User.builder()
                .username("alice")
                .password("plain-secret")
                .role(User.Role.RIDER)
                .build();
        when(passwordEncoder.encode("plain-secret")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(USER_ID);
            return saved;
        });

        userService.createUser(incoming);

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("$2a$hashed", saved.getPassword());
        assertNotEquals("plain-secret", saved.getPassword());
        assertEquals(USER_ID, saved.getId());

        verify(userCreatedKafkaProducer).sendUserCreatedEvent(eventCaptor.capture());
        UserCreatedEvent event = eventCaptor.getValue();
        assertEquals(USER_ID.toString(), event.userId());
        assertEquals("alice", event.username());
        assertEquals("RIDER", event.role());
    }

}
