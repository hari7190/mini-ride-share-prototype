package com.zamorincorp.rideshare.auth.producer;
//this class will be used to produce a Kafka event when a user is created
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.zamorincorp.rideshare.auth.dto.UserCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.user-created}")
    private String userCreatedTopic;

    @Value("${app.kafka.events.user-created.type}")
    private String userCreatedEventType;

    @Value("${app.kafka.events.user-created.version}")
    private String userCreatedEventVersion;

    public void sendUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(userCreatedEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize user created event", e);
        }
        Message<String> eventMessage = MessageBuilder.withPayload(payload)
        .setHeader(KafkaHeaders.TOPIC, userCreatedTopic)
        .setHeader(KafkaHeaders.KEY, userCreatedEvent.userId())
        .setHeader("eventType", userCreatedEventType)
        .setHeader("eventVersion", userCreatedEventVersion)
        .build();
        kafkaTemplate.send(eventMessage);
        log.info("User created event sent for userId={}", userCreatedEvent.userId());
    }
}
