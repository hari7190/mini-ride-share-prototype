package com.zamorincorp.rideshare.driver.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.driver.dto.RideDispatchEvent;
import com.zamorincorp.rideshare.driver.service.DriverService;
import com.zamorincorp.rideshare.driver.dto.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchKafkaListener {
    private final ObjectMapper objectMapper;
    private final DriverService driverService;

    @KafkaListener(topics = "${app.kafka.topics.driver-assigned}")
    public void onDriverAssigned(String payload) {
        try {
            RideDispatchEvent event = objectMapper.readValue(payload, RideDispatchEvent.class);
            driverService.handleDriverAssigned(event);
        } catch (Exception e) {
            log.error("Failed to process driver assigned message payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.user-created}")
    public void onUserCreated(String payload) {
        try {
            UserCreatedEvent event = objectMapper.readValue(payload, UserCreatedEvent.class);
            driverService.handleUserCreated(event);
        } catch (Exception e) {
            log.error("Failed to process user created message payload={}", payload, e);
        }
    }
}
