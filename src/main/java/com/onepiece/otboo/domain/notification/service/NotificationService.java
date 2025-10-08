package com.onepiece.otboo.domain.notification.service;

import java.util.UUID;

public interface NotificationService {

    void create(UUID userId, String title, String content);
}
