package com.cyboul.eda.paymentservice;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRecordRepository extends MongoRepository<PaymentRecord, String> {}
