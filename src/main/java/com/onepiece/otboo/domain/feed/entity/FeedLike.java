package com.onepiece.otboo.domain.feed.entity;

import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "feed_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "feed_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedLike {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder
    private FeedLike(UUID id, User user, Feed feed, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.feed = feed;
        this.createdAt = createdAt;
    }
}
