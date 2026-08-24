package com.orderservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import com.orderservice.event.UserSecurityVersionChangedEvent;

@Configuration
public class UserSecurityKafkaConsumerConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<
            String, UserSecurityVersionChangedEvent> userSecurityKafkaListenerContainerFactory(
            @Value(
                    "${spring.kafka.bootstrap-servers}"
            ) String bootstrapServers) {

        Map<String, Object> properties
                = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-user-security"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        ErrorHandlingDeserializer<
                UserSecurityVersionChangedEvent> valueDeserializer
                = new ErrorHandlingDeserializer<>(
                        new JacksonJsonDeserializer<>(
                                UserSecurityVersionChangedEvent.class,
                                false
                        )
                );

        DefaultKafkaConsumerFactory<
                String, UserSecurityVersionChangedEvent> consumerFactory
                = new DefaultKafkaConsumerFactory<>(
                        properties,
                        new StringDeserializer(),
                        valueDeserializer
                );

        ConcurrentKafkaListenerContainerFactory<
                String, UserSecurityVersionChangedEvent> factory
                = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
