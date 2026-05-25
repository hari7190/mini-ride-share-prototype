package com.zamorincorp.rideshare.auth.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.auth.dto.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreatedKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserCreatedKafkaProducer producer;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "userCreatedTopic", "user-created.v1");
        ReflectionTestUtils.setField(producer, "userCreatedEventType",
                "com.zamorincorp.rideshare.auth.dto.UserCreatedEvent");
        ReflectionTestUtils.setField(producer, "userCreatedEventVersion", "1.0.0");
    }

    @Test
    void sendUserCreatedEvent_publishesMessageWithHeaders() throws Exception {
        UserCreatedEvent event = sampleEvent();
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"userId\":\"1\"}");

        producer.sendUserCreatedEvent(event);

        verify(kafkaTemplate).send(messageCaptor.capture());
        Message<String> message = messageCaptor.getValue();
        assertEquals("{\"userId\":\"1\"}", message.getPayload());
        assertEquals("user-created.v1", message.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("user-1", message.getHeaders().get(KafkaHeaders.KEY));
        assertEquals("com.zamorincorp.rideshare.auth.dto.UserCreatedEvent",
                message.getHeaders().get("eventType"));
        assertEquals("1.0.0", message.getHeaders().get("eventVersion"));
    }

    @Test
    void sendUserCreatedEvent_whenSerializationFails_throwsIllegalStateException() throws Exception {
        UserCreatedEvent event = sampleEvent();
        doThrow(new JsonProcessingException("boom") { })
                .when(objectMapper).writeValueAsString(event);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> producer.sendUserCreatedEvent(event));
        assertTrue(ex.getMessage().contains("Could not serialize user created event"));
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    private UserCreatedEvent sampleEvent() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 12, 0);
        return new UserCreatedEvent("user-1", "alice", "RIDER", now, now);
    }

}
