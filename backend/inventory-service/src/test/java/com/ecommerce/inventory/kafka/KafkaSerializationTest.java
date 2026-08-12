package com.ecommerce.inventory.kafka;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import com.ecommerce.inventory.event.InventoryRejectionReason;
import com.ecommerce.inventory.event.InventoryResultEvent;
import com.ecommerce.inventory.event.InventoryResultStatus;

class KafkaSerializationTest {

    @Test
    void shouldPreserveRejectionReasonDuringSerialization() {
        UUID resultEventId = UUID.randomUUID();
        UUID causationId = UUID.randomUUID();
        Instant occurredAt = Instant.parse(
                "2026-08-02T10:00:00Z"
        );

        InventoryResultEvent original
                = new InventoryResultEvent(
                        resultEventId,
                        "InventoryResult",
                        1,
                        causationId,
                        106L,
                        InventoryResultStatus.REJECTED,
                        InventoryRejectionReason.INSUFFICIENT_STOCK,
                        "Insufficient stock for product 10",
                        List.of(),
                        null,
                        occurredAt
                );

        JacksonJsonSerializer<InventoryResultEvent> serializer
                = new JacksonJsonSerializer<>();

        JacksonJsonDeserializer<InventoryResultEvent> deserializer
                = new JacksonJsonDeserializer<>(
                        InventoryResultEvent.class
                );

        try {
            byte[] serialized = serializer.serialize(
                    "inventory.result.v1",
                    original
            );

            String json = new String(
                    serialized,
                    StandardCharsets.UTF_8
            );

            InventoryResultEvent restored
                    = deserializer.deserialize(
                            "inventory.result.v1",
                            serialized
                    );

            assertThat(serialized).isNotEmpty();

            /*
             * Verify the outgoing Kafka JSON contains the field.
             */
            assertThat(json)
                    .contains(
                            "\"status\":\"REJECTED\""
                    )
                    .contains(
                            "\"rejectionReason\":\"INSUFFICIENT_STOCK\""
                    );

            /*
             * Verify the event can be reconstructed from JSON.
             */
            assertThat(restored).isNotNull();
            assertThat(restored.eventId())
                    .isEqualTo(resultEventId);

            assertThat(restored.causationId())
                    .isEqualTo(causationId);

            assertThat(restored.orderId())
                    .isEqualTo(106L);

            assertThat(restored.status())
                    .isEqualTo(
                            InventoryResultStatus.REJECTED
                    );

            assertThat(restored.rejectionReason())
                    .isEqualTo(
                            InventoryRejectionReason.INSUFFICIENT_STOCK
                    );

            assertThat(restored.message())
                    .isEqualTo(
                            "Insufficient stock for product 10"
                    );

            assertThat(restored.items()).isEmpty();
            assertThat(restored.totalAmount()).isNull();
            assertThat(restored.occurredAt())
                    .isEqualTo(occurredAt);
        } finally {
            serializer.close();
            deserializer.close();
        }
    }
}
