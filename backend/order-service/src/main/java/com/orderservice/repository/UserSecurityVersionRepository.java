package com.orderservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderservice.entity.UserSecurityVersion;

public interface UserSecurityVersionRepository
        extends JpaRepository<
            UserSecurityVersion, Long> {

    boolean existsByLastEventId(UUID eventId);
}
