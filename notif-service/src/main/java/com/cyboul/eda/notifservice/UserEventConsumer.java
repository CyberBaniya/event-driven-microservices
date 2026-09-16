package com.cyboul.eda.notifservice;

import com.cyboul.eda.common.events.UserCreatedEvent;
import com.cyboul.eda.common.events.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "user-events", groupId = "${spring.kafka.consumer.group-id}")
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final UserContactRepository userContactRepository;

    @KafkaHandler
    public void onUserCreated(UserCreatedEvent event) {
        log.info("NOTIF: Received UserCreatedEvent for userId={}", event.userId());
        userContactRepository.save(new UserContactProjection(event.userId(), event.email(), event.name())).block();
    }

    @KafkaHandler
    public void onUserUpdated(UserUpdatedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String userId) {
        log.info("NOTIF: Received UserUpdatedEvent for userId={}", userId);
        userContactRepository.findById(userId)
                .doOnNext(p -> p.setName(event.name()))
                .flatMap(userContactRepository::save)
                .block();
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object event) {
        log.warn("NOTIF: Received unknown event on user-events topic: {}", event.getClass().getName());
    }
}
