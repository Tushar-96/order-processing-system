package com.ecommerce.inventory.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.inventory.event.InventoryRejectionReason;
import com.ecommerce.inventory.event.InventoryResultEvent;
import com.ecommerce.inventory.event.InventoryResultStatus;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.event.OrderItemRequested;
import com.ecommerce.inventory.integration.PostgresIntegrationTest;
import com.ecommerce.inventory.model.InventoryReservation;
import com.ecommerce.inventory.model.Product;
import com.ecommerce.inventory.model.ReservationStatus;
import com.ecommerce.inventory.repository.InventoryReservationRepository;
import com.ecommerce.inventory.repository.ProcessedEventRepository;
import com.ecommerce.inventory.repository.ProductRepository;

@Transactional
class InventoryReservationServiceTest
        extends PostgresIntegrationTest {

    @Autowired
    private InventoryReservationService reservationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void cleanDatabase() {
        processedEventRepository.deleteAll();
        reservationRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldRejectOrderWhenProductDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        Long missingProductId = 999999L;

        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                eventId,
                "OrderCreated",
                1,
                103L,
                503L,
                List.of(
                        new OrderItemRequested(
                                missingProductId,
                                1
                        )
                ),
                Instant.now()
        );

        InventoryResultEvent result
                = reservationService.process(orderEvent);

        InventoryReservation rejectedReservation
                = reservationRepository.findByOrderId(103L)
                        .orElseThrow();

        assertThat(result.status())
                .isEqualTo(InventoryResultStatus.REJECTED);

        assertThat(result.rejectionReason())
                .isEqualTo(
                        InventoryRejectionReason.PRODUCT_NOT_FOUND
                );

        assertThat(result.message())
                .contains("Product " + missingProductId)
                .contains("was not found");

        assertThat(result.orderId()).isEqualTo(103L);
        assertThat(result.causationId()).isEqualTo(eventId);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalAmount()).isNull();

        assertThat(rejectedReservation.getStatus())
                .isEqualTo(ReservationStatus.REJECTED);

        assertThat(rejectedReservation.getRejectionReason())
                .isEqualTo(
                        InventoryRejectionReason.PRODUCT_NOT_FOUND
                );

        assertThat(rejectedReservation.getItems()).isEmpty();

        assertThat(processedEventRepository.existsById(eventId))
                .isTrue();
    }

    @Test
    void shouldRejectOrderWhenStockIsInsufficient() {
        Product product = Product.builder()
                .name("Wireless Mouse")
                .description("Test mouse")
                .price(new BigDecimal("1000.00"))
                .availableQuantity(2)
                .build();

        product = productRepository.saveAndFlush(product);

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                eventId,
                "OrderCreated",
                1,
                102L,
                502L,
                List.of(
                        new OrderItemRequested(
                                product.getId(),
                                5
                        )
                ),
                Instant.now()
        );

        InventoryResultEvent result
                = reservationService.process(orderEvent);

        productRepository.flush();

        Product unchangedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        InventoryReservation rejectedReservation
                = reservationRepository.findByOrderId(102L)
                        .orElseThrow();

        assertThat(result.status())
                .isEqualTo(InventoryResultStatus.REJECTED);

        assertThat(result.rejectionReason())
                .isEqualTo(
                        InventoryRejectionReason.INSUFFICIENT_STOCK
                );

        assertThat(result.message())
                .contains("Insufficient stock");

        assertThat(result.orderId()).isEqualTo(102L);
        assertThat(result.causationId()).isEqualTo(eventId);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalAmount()).isNull();

        assertThat(unchangedProduct.getAvailableQuantity())
                .isEqualTo(2);

        assertThat(rejectedReservation.getStatus())
                .isEqualTo(ReservationStatus.REJECTED);

        assertThat(rejectedReservation.getRejectionReason())
                .isEqualTo(
                        InventoryRejectionReason.INSUFFICIENT_STOCK
                );

        assertThat(processedEventRepository.existsById(eventId))
                .isTrue();
    }

    @Test
    void shouldReserveAvailableStockSuccessfully() {
        Product product = Product.builder()
                .name("Mechanical Keyboard")
                .description("Test keyboard")
                .price(new BigDecimal("2500.00"))
                .availableQuantity(10)
                .build();

        product = productRepository.saveAndFlush(product);

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                eventId,
                "OrderCreated",
                1,
                101L,
                501L,
                List.of(
                        new OrderItemRequested(
                                product.getId(),
                                3
                        )
                ),
                Instant.now()
        );

        InventoryResultEvent result
                = reservationService.process(orderEvent);

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        InventoryReservation savedReservation
                = reservationRepository.findByOrderId(101L)
                        .orElseThrow();

        assertThat(result.status())
                .isEqualTo(InventoryResultStatus.RESERVED);

        assertThat(result.rejectionReason()).isNull();

        assertThat(result.message())
                .isEqualTo("Inventory reserved successfully");

        assertThat(result.orderId()).isEqualTo(101L);
        assertThat(result.causationId()).isEqualTo(eventId);

        assertThat(result.totalAmount())
                .isEqualByComparingTo("7500.00");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productId())
                .isEqualTo(product.getId());

        assertThat(result.items().get(0).quantity())
                .isEqualTo(3);

        assertThat(updatedProduct.getAvailableQuantity())
                .isEqualTo(7);

        assertThat(savedReservation.getItems()).hasSize(1);

        assertThat(processedEventRepository.existsById(eventId))
                .isTrue();
    }

    @Test
    void shouldNotReserveStockTwiceForDuplicateEvent() {
        Product product = Product.builder()
                .name("Gaming Monitor")
                .description("Test monitor")
                .price(new BigDecimal("15000.00"))
                .availableQuantity(10)
                .build();

        product = productRepository.saveAndFlush(product);

        UUID sourceEventId = UUID.randomUUID();

        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                sourceEventId,
                "OrderCreated",
                1,
                104L,
                504L,
                List.of(
                        new OrderItemRequested(
                                product.getId(),
                                2
                        )
                ),
                Instant.now()
        );

        InventoryResultEvent firstResult
                = reservationService.process(orderEvent);

        InventoryResultEvent duplicateResult
                = reservationService.process(orderEvent);

        productRepository.flush();

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(firstResult.status())
                .isEqualTo(InventoryResultStatus.RESERVED);

        assertThat(duplicateResult.status())
                .isEqualTo(InventoryResultStatus.RESERVED);

        /*
     * The duplicate invocation must return the original result,
     * not generate another inventory-result event.
         */
        assertThat(duplicateResult.eventId())
                .isEqualTo(firstResult.eventId());

        assertThat(duplicateResult.causationId())
                .isEqualTo(sourceEventId);

        /*
     * Initial stock is 10 and the order requests 2.
     * Correct idempotent result is 8, not 6.
         */
        assertThat(updatedProduct.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(reservationRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.count()).isEqualTo(1);

        InventoryReservation savedReservation
                = reservationRepository.findByOrderId(104L)
                        .orElseThrow();

        assertThat(savedReservation.getItems()).hasSize(1);

        assertThat(savedReservation.getItems().get(0).getQuantity())
                .isEqualTo(2);
    }

    @Test
    void shouldNotChangeAnyStockWhenOneProductHasInsufficientStock() {
        Product availableProduct = Product.builder()
                .name("Laptop")
                .description("Product with sufficient stock")
                .price(new BigDecimal("60000.00"))
                .availableQuantity(5)
                .build();

        Product insufficientProduct = Product.builder()
                .name("USB Headset")
                .description("Product with insufficient stock")
                .price(new BigDecimal("3000.00"))
                .availableQuantity(1)
                .build();

        availableProduct
                = productRepository.saveAndFlush(availableProduct);

        insufficientProduct
                = productRepository.saveAndFlush(insufficientProduct);

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                eventId,
                "OrderCreated",
                1,
                105L,
                505L,
                List.of(
                        new OrderItemRequested(
                                availableProduct.getId(),
                                2
                        ),
                        new OrderItemRequested(
                                insufficientProduct.getId(),
                                5
                        )
                ),
                Instant.now()
        );

        InventoryResultEvent result
                = reservationService.process(orderEvent);

        productRepository.flush();

        Product availableProductAfter = productRepository
                .findById(availableProduct.getId())
                .orElseThrow();

        Product insufficientProductAfter = productRepository
                .findById(insufficientProduct.getId())
                .orElseThrow();

        InventoryReservation rejectedReservation
                = reservationRepository.findByOrderId(105L)
                        .orElseThrow();

        assertThat(result.status())
                .isEqualTo(InventoryResultStatus.REJECTED);

        assertThat(result.rejectionReason())
                .isEqualTo(
                        InventoryRejectionReason.INSUFFICIENT_STOCK
                );

        /*
     * Neither product should be modified.
         */
        assertThat(availableProductAfter.getAvailableQuantity())
                .isEqualTo(5);

        assertThat(insufficientProductAfter.getAvailableQuantity())
                .isEqualTo(1);

        /*
     * A rejected reservation should not contain reserved items.
         */
        assertThat(result.items()).isEmpty();
        assertThat(rejectedReservation.getItems()).isEmpty();

        assertThat(rejectedReservation.getStatus())
                .isEqualTo(ReservationStatus.REJECTED);

        assertThat(reservationRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.existsById(eventId))
                .isTrue();
    }
}
