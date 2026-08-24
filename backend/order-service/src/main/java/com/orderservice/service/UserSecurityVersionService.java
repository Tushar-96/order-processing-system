package com.orderservice.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderservice.entity.UserSecurityVersion;
import com.orderservice.event.UserSecurityVersionChangedEvent;
import com.orderservice.repository.UserSecurityVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityVersionService {

    private final UserSecurityVersionRepository repository;

    @Transactional
    public void process(
            UserSecurityVersionChangedEvent event) {

        if (repository.existsByLastEventId(
                event.eventId()
        )) {
            return;
        }

        UserSecurityVersion stored
                = repository.findById(event.userId())
                        .orElseGet(()
                                -> UserSecurityVersion.builder()
                                .userId(event.userId())
                                .securityVersion(0L)
                                .build()
                        );

        /*
         * Ignore old or out-of-order events.
         */
        if (event.securityVersion()
                <= stored.getSecurityVersion()) {
            return;
        }

        stored.setSecurityVersion(
                event.securityVersion()
        );

        stored.setLastEventId(event.eventId());
        stored.setUpdatedAt(Instant.now());

        repository.save(stored);
    }

    @Transactional(readOnly = true)
    public long getCurrentVersion(Long userId) {
        return repository.findById(userId)
                .map(
                        UserSecurityVersion::getSecurityVersion
                )
                .orElse(0L);
    }

    public long getSecurityVersion(Long userId) {
        return repository
                .findById(userId)
                .map(UserSecurityVersion::getSecurityVersion)
                .orElse(0L);
    }
}
