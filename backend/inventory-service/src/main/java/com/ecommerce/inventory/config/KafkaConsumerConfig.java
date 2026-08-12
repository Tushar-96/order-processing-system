package com.ecommerce.inventory.config;

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

import com.ecommerce.inventory.event.OrderCancelledEvent;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<
            String, OrderCancelledEvent> orderCancelledKafkaListenerContainerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "inventory-cancellation-service"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        ErrorHandlingDeserializer<OrderCancelledEvent> valueDeserializer
                = new ErrorHandlingDeserializer<>(
                        new JacksonJsonDeserializer<>(
                                OrderCancelledEvent.class,
                                false
                        )
                );

        DefaultKafkaConsumerFactory<
                String, OrderCancelledEvent> consumerFactory
                = new DefaultKafkaConsumerFactory<>(
                        properties,
                        new StringDeserializer(),
                        valueDeserializer
                );

        ConcurrentKafkaListenerContainerFactory<
                String, OrderCancelledEvent> factory
                = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
