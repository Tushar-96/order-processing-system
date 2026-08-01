package com.ecommerce.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.inventory.model.InventoryReservation;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderId(Long orderId);

    Optional<InventoryReservation> findBySourceEventId(
            UUID sourceEventId
    );
    Optional<InventoryReservation> findByResultEventId(
        UUID resultEventId
);

    boolean existsByOrderId(Long orderId);

    boolean existsBySourceEventId(UUID sourceEventId);
}
