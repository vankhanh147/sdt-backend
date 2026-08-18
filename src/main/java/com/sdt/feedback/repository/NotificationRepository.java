package com.sdt.feedback.repository;

import com.sdt.feedback.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    long countByIsReadFalse();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification notification
            set notification.isRead = true,
                notification.readAt = :readAt
            where notification.isRead = false
            """)
    int markAllUnreadAsRead(@Param("readAt") OffsetDateTime readAt);
}
