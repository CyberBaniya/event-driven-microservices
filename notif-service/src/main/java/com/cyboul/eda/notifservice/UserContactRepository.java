package com.cyboul.eda.notifservice;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserContactRepository extends ReactiveMongoRepository<UserContactProjection, String> {}
