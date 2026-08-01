package com.ecommerce.inventory.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.inventory.model.ProcessedEvent;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, UUID> {
}
