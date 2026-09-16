package com.cyboul.eda.notifservice;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final Sinks.Many<NotificationEvent> notificationSink;

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> stream() {
        // Server-Sent Events (SSE) = Unidirectional server → client
        log.info("NOTIF: Streaming events for Frontend");

        return notificationSink.asFlux()
                .map(n -> ServerSentEvent.builder()
                        .event(n.type())
                        .data(n.payload())
                        .build());
    }
}
