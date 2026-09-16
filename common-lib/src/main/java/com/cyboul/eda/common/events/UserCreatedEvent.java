package com.cyboul.eda.common.events;

public record UserCreatedEvent(
        String userId,
        String email,
        String name
) {}
