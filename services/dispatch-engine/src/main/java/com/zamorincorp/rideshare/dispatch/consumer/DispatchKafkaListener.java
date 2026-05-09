package com.zamorincorp.rideshare.dispatch.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.dispatch.dto.RideRequestedEvent;
import com.zamorincorp.rideshare.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchKafkaListener {

    private final ObjectMapper objectMapper;
    private final DispatchService dispatchService;

    @KafkaListener(topics = "${app.kafka.topics.ride-requests}")
    public void onRideRequested(
            String payload,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        try {
            RideRequestedEvent event = objectMapper.readValue(payload, RideRequestedEvent.class);
            dispatchService.handleRideRequested(event);
        } catch (Exception e) {
            log.error("Failed to process ride request message key={} payload={}", key, payload, e);
        }
    }
}
