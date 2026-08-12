package com.ecommerce.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.inventory.model.InventoryReservation;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    @Query("""
        select distinct reservation
        from InventoryReservation reservation
        left join fetch reservation.items
        where reservation.orderId = :orderId
        """)
    Optional<InventoryReservation> findByOrderIdWithItems(
            @Param("orderId") Long orderId
    );

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
