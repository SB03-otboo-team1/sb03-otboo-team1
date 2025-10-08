package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.enums.Level;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {

    void create(Set<UUID> userId, String title, String content, Level level);
}
