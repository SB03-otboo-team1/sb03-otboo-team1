package com.onepiece.otboo.domain.feed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "feed_clothes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"feed_id","clothes_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedClothes {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id; // 단일 PK

    @Column(name = "feed_id", nullable = false, columnDefinition = "uuid")
    private UUID feedId;

    @Column(name = "clothes_id", nullable = false, columnDefinition = "uuid")
    private UUID clothesId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}