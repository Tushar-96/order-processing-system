package com.orderservice.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_security_versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurityVersion {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(
            name = "security_version",
            nullable = false
    )
    private long securityVersion;

    @Column(
            name = "last_event_id",
            nullable = false,
            unique = true
    )
    private UUID lastEventId;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;
}
