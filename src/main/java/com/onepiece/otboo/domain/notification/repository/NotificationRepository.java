package com.onepiece.otboo.domain.notification.repository;

import com.onepiece.otboo.domain.notification.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>,
    NotificationRepositoryCustom {

    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(UUID receiverId);

}