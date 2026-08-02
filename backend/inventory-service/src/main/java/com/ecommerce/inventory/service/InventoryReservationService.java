package com.ecommerce.inventory.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.inventory.event.InventoryRejectionReason;
import com.ecommerce.inventory.event.InventoryResultEvent;
import com.ecommerce.inventory.event.InventoryResultStatus;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.event.OrderItemRequested;
import com.ecommerce.inventory.event.ReservedInventoryItem;
import com.ecommerce.inventory.exception.InvalidOrderEventException;
import com.ecommerce.inventory.model.InventoryReservation;
import com.ecommerce.inventory.model.InventoryReservationItem;
import com.ecommerce.inventory.model.ProcessedEvent;
import com.ecommerce.inventory.model.Product;
import com.ecommerce.inventory.model.ReservationStatus;
import com.ecommerce.inventory.repository.InventoryReservationRepository;
import com.ecommerce.inventory.repository.ProcessedEventRepository;
import com.ecommerce.inventory.repository.ProductRepository;

@Service
public class InventoryReservationService {

    private static final String ORDER_CREATED_TYPE = "OrderCreated";
    private static final String INVENTORY_RESULT_TYPE = "InventoryResult";
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProcessedEventRepository processedEventRepository;

    public InventoryReservationService(
            ProductRepository productRepository,
            InventoryReservationRepository reservationRepository,
            ProcessedEventRepository processedEventRepository) {

        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public InventoryResultEvent process(OrderCreatedEvent event) {
        validateEnvelope(event);

        Optional<ProcessedEvent> processedEvent =
                processedEventRepository.findById(event.eventId());

        if (processedEvent.isPresent()) {
            return loadExistingResult(
                    processedEvent.get().getOrderId()
            );
        }

        Optional<InventoryReservation> existingReservation =
                reservationRepository.findByOrderId(event.orderId());

        if (existingReservation.isPresent()) {
            markProcessed(event);
            return toResultEvent(existingReservation.get());
        }

        Optional<String> itemValidationError =
                validateItems(event.items());

        if (itemValidationError.isPresent()) {
            return reject(
                    event,
                    InventoryRejectionReason.INVALID_ORDER,
                    itemValidationError.get()
            );
        }

        List<OrderItemRequested> sortedItems = event.items()
                .stream()
                .sorted(Comparator.comparing(
                        OrderItemRequested::productId
                ))
                .toList();

        List<LockedProductRequest> lockedRequests =
                new ArrayList<>();

        for (OrderItemRequested requestedItem : sortedItems) {
            Optional<Product> product =
                    productRepository.findByIdForUpdate(
                            requestedItem.productId()
                    );

            if (product.isEmpty()) {
                return reject(
                        event,
                        InventoryRejectionReason.PRODUCT_NOT_FOUND,
                        "Product " + requestedItem.productId()
                                + " was not found"
                );
            }

            lockedRequests.add(
                    new LockedProductRequest(
                            product.get(),
                            requestedItem.quantity()
                    )
            );
        }

        for (LockedProductRequest request : lockedRequests) {
            Product product = request.product();

            if (product.getAvailableQuantity() < request.quantity()) {
                return reject(
                        event,
                        InventoryRejectionReason.INSUFFICIENT_STOCK,
                        "Insufficient stock for product "
                                + product.getId()
                );
            }
        }

        return reserve(event, lockedRequests);
    }

    private InventoryResultEvent reserve(
            OrderCreatedEvent event,
            List<LockedProductRequest> lockedRequests) {

        InventoryReservation reservation =
                InventoryReservation.builder()
                        .orderId(event.orderId())
                        .sourceEventId(event.eventId())
                        .resultEventId(UUID.randomUUID())
                        .status(ReservationStatus.RESERVED)
                        .message("Inventory reserved successfully")
                        .totalAmount(BigDecimal.ZERO)
                        .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (LockedProductRequest request : lockedRequests) {
            Product product = request.product();
            int quantity = request.quantity();

            product.setAvailableQuantity(
                    product.getAvailableQuantity() - quantity
            );

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(itemTotal);

            InventoryReservationItem reservationItem =
                    InventoryReservationItem.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .quantity(quantity)
                            .unitPrice(product.getPrice())
                            .build();

            reservation.addItem(reservationItem);
        }

        reservation.setTotalAmount(totalAmount);

        InventoryReservation savedReservation =
                reservationRepository.save(reservation);

        markProcessed(event);

        return toResultEvent(savedReservation);
    }

    private InventoryResultEvent reject(
            OrderCreatedEvent event,
            InventoryRejectionReason reason,
            String message) {

        InventoryReservation reservation =
                InventoryReservation.builder()
                        .orderId(event.orderId())
                        .sourceEventId(event.eventId())
                        .resultEventId(UUID.randomUUID())
                        .status(ReservationStatus.REJECTED)
                        .rejectionReason(reason)
                        .message(message)
                        .totalAmount(null)
                        .build();

        InventoryReservation savedReservation =
                reservationRepository.save(reservation);

        markProcessed(event);

        return toResultEvent(savedReservation);
    }

    private void markProcessed(OrderCreatedEvent event) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(event.eventId())
                .eventType(event.eventType())
                .orderId(event.orderId())
                .processedAt(Instant.now())
                .build();

        processedEventRepository.save(processedEvent);
    }

    private InventoryResultEvent loadExistingResult(Long orderId) {
        InventoryReservation reservation =
                reservationRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Processed event exists without "
                                        + "an inventory reservation"
                        ));

        return toResultEvent(reservation);
    }

    private InventoryResultEvent toResultEvent(
            InventoryReservation reservation) {

        List<ReservedInventoryItem> items =
                reservation.getItems()
                        .stream()
                        .map(item -> new ReservedInventoryItem(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .toList();

        InventoryResultStatus resultStatus =
                InventoryResultStatus.valueOf(
                        reservation.getStatus().name()
                );

        return new InventoryResultEvent(
                reservation.getResultEventId(),
                INVENTORY_RESULT_TYPE,
                SUPPORTED_EVENT_VERSION,
                reservation.getSourceEventId(),
                reservation.getOrderId(),
                resultStatus,
                reservation.getRejectionReason(),
                reservation.getMessage(),
                items,
                reservation.getTotalAmount(),
                reservation.getCreatedAt()
        );
    }

    private void validateEnvelope(OrderCreatedEvent event) {
        if (event == null) {
            throw new InvalidOrderEventException(
                    "Order event is required"
            );
        }

        if (event.eventId() == null) {
            throw new InvalidOrderEventException(
                    "Order event ID is required"
            );
        }

        if (!ORDER_CREATED_TYPE.equals(event.eventType())) {
            throw new InvalidOrderEventException(
                    "Unsupported event type"
            );
        }

        if (event.version() != SUPPORTED_EVENT_VERSION) {
            throw new InvalidOrderEventException(
                    "Unsupported event version: " + event.version()
            );
        }

        if (event.orderId() == null || event.orderId() <= 0) {
            throw new InvalidOrderEventException(
                    "A positive order ID is required"
            );
        }

        if (event.userId() == null || event.userId() <= 0) {
            throw new InvalidOrderEventException(
                    "A positive user ID is required"
            );
        }

        if (event.occurredAt() == null) {
            throw new InvalidOrderEventException(
                    "Event timestamp is required"
            );
        }
    }

    private Optional<String> validateItems(
            List<OrderItemRequested> items) {

        if (items == null || items.isEmpty()) {
            return Optional.of(
                    "Order must contain at least one item"
            );
        }

        Set<Long> productIds = new HashSet<>();

        for (OrderItemRequested item : items) {
            if (item == null) {
                return Optional.of(
                        "Order contains a null item"
                );
            }

            if (item.productId() == null
                    || item.productId() <= 0) {

                return Optional.of(
                        "Every product ID must be positive"
                );
            }

            if (item.quantity() <= 0) {
                return Optional.of(
                        "Every quantity must be greater than zero"
                );
            }

            if (!productIds.add(item.productId())) {
                return Optional.of(
                        "Duplicate product ID: "
                                + item.productId()
                );
            }
        }

        return Optional.empty();
    }

    private record LockedProductRequest(
            Product product,
            int quantity
    ) {
    }
}
