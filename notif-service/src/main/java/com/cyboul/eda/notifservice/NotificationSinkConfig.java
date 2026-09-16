package com.cyboul.eda.notifservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class NotificationSinkConfig {

    @Bean
    public Sinks.Many<NotificationEvent> notificationSink() {
        return Sinks.many().multicast().onBackpressureBuffer(256, false);
    }
}
