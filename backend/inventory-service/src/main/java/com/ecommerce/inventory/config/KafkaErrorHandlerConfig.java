package com.ecommerce.inventory.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

import com.ecommerce.inventory.exception.InvalidOrderEventException;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${application.kafka.topics.order-created-dlt}") String deadLetterTopic) {

        DeadLetterPublishingRecoverer recoverer
                = new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception)
                        -> new TopicPartition(
                                deadLetterTopic,
                                record.partition()
                        )
                );

        // Retry twice, waiting one second between attempts.
        FixedBackOff backOff = new FixedBackOff(1000L, 2L);

        DefaultErrorHandler errorHandler
                = new DefaultErrorHandler(recoverer, backOff);

        // Retrying malformed or unsupported events cannot fix them.
        errorHandler.addNotRetryableExceptions(
                InvalidOrderEventException.class,
                DeserializationException.class
        );

        return errorHandler;
    }
}
