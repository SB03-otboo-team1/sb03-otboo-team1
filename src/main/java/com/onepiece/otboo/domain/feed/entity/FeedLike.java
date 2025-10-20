package com.onepiece.otboo.domain.feed.entity;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_likes",
    uniqueConstraints = @UniqueConstraint(name = "uk_feed_likes_user_feed",
        columnNames = {"user_id", "feed_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Builder(access = AccessLevel.PRIVATE)
    private FeedLike(User user, Feed feed) {
        this.user = user;
        this.feed = feed;
    }

    public static FeedLike of(User user, Feed feed) {
        return FeedLike.builder().user(user).feed(feed).build();
    }
}

