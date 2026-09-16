package com.cyboul.eda.productservice;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.ProducerListener;

@Configuration
public class KafkaProducerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Bean
    public ProducerListener<Object, Object> kafkaProducerListener() {
        return new ProducerListener<>() {
            @Override
            public void onError(ProducerRecord<Object, Object> record, RecordMetadata metadata, Exception ex) {
                log.error("Failed to publish to topic {}: {}", record.topic(), ex.getMessage());
            }
        };
    }
}
