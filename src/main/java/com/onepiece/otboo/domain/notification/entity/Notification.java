package com.onepiece.otboo.domain.notification.entity;

import com.onepiece.otboo.domain.notification.enums.NotificationLevel;
import com.onepiece.otboo.global.base.BaseEntity;
import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseUpdatableEntity {

    @Column(nullable = false)
    private UUID receiverId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationLevel level;

    @Builder
    public Notification(UUID receiverId, String title, String content, NotificationLevel level) {
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.level = level;
        if (getCreatedAt() == null) {
            try {
                var field = BaseEntity.class.getDeclaredField("createdAt");
                field.setAccessible(true);
                field.set(this, java.time.Instant.now());
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}