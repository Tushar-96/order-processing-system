package com.ecommerce.inventory.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.inventory.event.InventoryResultEvent;
import com.ecommerce.inventory.event.InventoryResultStatus;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.event.OrderItemRequested;
import com.ecommerce.inventory.event.ReservedInventoryItem;
import com.ecommerce.inventory.service.InventoryReservationService;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventConsumerTest {

    @Mock
    private InventoryReservationService reservationService;

    @Mock
    private InventoryResultProducer resultProducer;

    @InjectMocks
    private OrderCreatedEventConsumer consumer;

    @Test
    void shouldProcessOrderAndPublishInventoryResult() {
        UUID sourceEventId = UUID.randomUUID();
        UUID resultEventId = UUID.randomUUID();

        OrderCreatedEvent orderEvent
                = new OrderCreatedEvent(
                        sourceEventId,
                        "OrderCreated",
                        1,
                        201L,
                        601L,
                        List.of(
                                new OrderItemRequested(
                                        10L,
                                        2
                                )
                        ),
                        Instant.parse(
                                "2026-08-02T10:00:00Z"
                        )
                );

        InventoryResultEvent expectedResult
                = new InventoryResultEvent(
                        resultEventId,
                        "InventoryResult",
                        1,
                        sourceEventId,
                        201L,
                        InventoryResultStatus.RESERVED,
                        null,
                        "Inventory reserved successfully",
                        List.of(
                                new ReservedInventoryItem(
                                        10L,
                                        "Mechanical Keyboard",
                                        2,
                                        new BigDecimal("2500.00")
                                )
                        ),
                        new BigDecimal("5000.00"),
                        Instant.parse(
                                "2026-08-02T10:00:01Z"
                        )
                );

        when(reservationService.process(orderEvent))
                .thenReturn(expectedResult);

        consumer.consume(orderEvent);

        verify(reservationService).process(orderEvent);
        verify(resultProducer).publish(expectedResult);

        /*
         * Also verify processing occurs before publishing.
         */
        var orderedCalls = inOrder(
                reservationService,
                resultProducer
        );

        orderedCalls.verify(reservationService)
                .process(orderEvent);

        orderedCalls.verify(resultProducer)
                .publish(expectedResult);
    }

    @Test
    void shouldNotPublishWhenReservationProcessingFails() {
        OrderCreatedEvent orderEvent
                = new OrderCreatedEvent(
                        UUID.randomUUID(),
                        "OrderCreated",
                        1,
                        202L,
                        602L,
                        List.of(
                                new OrderItemRequested(
                                        20L,
                                        1
                                )
                        ),
                        Instant.now()
                );

        when(reservationService.process(orderEvent))
                .thenThrow(
                        new RuntimeException(
                                "Database processing failed"
                        )
                );

        assertThatThrownBy(
                () -> consumer.consume(orderEvent)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database processing failed");

        verify(reservationService).process(orderEvent);
        verifyNoInteractions(resultProducer);
    }
}
