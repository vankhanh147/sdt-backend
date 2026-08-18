package com.sdt.feedback.service;

import com.sdt.feedback.dto.response.MarkAllNotificationsReadResponse;
import com.sdt.feedback.dto.response.NotificationResponse;
import com.sdt.feedback.dto.response.PageResponse;
import com.sdt.feedback.dto.response.UnreadNotificationCountResponse;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.Notification;
import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.enums.NotificationType;
import com.sdt.feedback.exception.InvalidFilterException;
import com.sdt.feedback.exception.ResourceNotFoundException;
import com.sdt.feedback.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String NEW_FEEDBACK_TITLE = "Phản hồi mới";
    private static final String NEW_FEEDBACK_MESSAGE =
            "Có một phản hồi mới được gửi vào hệ thống.";
    private static final String STATUS_CHANGED_TITLE =
            "Trạng thái phản hồi đã thay đổi";

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(int page, int size) {
        validatePage(page, size);
        Page<Notification> notificationPage = notificationRepository.findAll(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.desc("createdAt"),
                                Sort.Order.desc("id")
                        )
                )
        );
        List<NotificationResponse> content = notificationPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isFirst(),
                notificationPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount() {
        return new UnreadNotificationCountResponse(
                notificationRepository.countByIsReadFalse()
        );
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id=" + id
                ));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(now());
            notificationRepository.saveAndFlush(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public MarkAllNotificationsReadResponse markAllAsRead() {
        int updatedCount = notificationRepository.markAllUnreadAsRead(now());
        return new MarkAllNotificationsReadResponse(updatedCount);
    }

    @Transactional
    public void createNewFeedbackNotification(Feedback feedback) {
        create(
                NotificationType.NEW_FEEDBACK,
                NEW_FEEDBACK_TITLE,
                NEW_FEEDBACK_MESSAGE,
                feedback
        );
    }

    @Transactional
    public void createStatusChangedNotification(
            Feedback feedback,
            FeedbackStatus previousStatus,
            FeedbackStatus newStatus
    ) {
        create(
                NotificationType.FEEDBACK_STATUS_CHANGED,
                STATUS_CHANGED_TITLE,
                "Trạng thái phản hồi đã thay đổi từ %s sang %s."
                        .formatted(previousStatus, newStatus),
                feedback
        );
    }

    private void create(
            NotificationType type,
            String title,
            String message,
            Feedback feedback
    ) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedFeedback(feedback);
        notification.setIsRead(false);
        notification.setReadAt(null);
        notificationRepository.saveAndFlush(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        Feedback relatedFeedback = notification.getRelatedFeedback();
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                relatedFeedback == null ? null : relatedFeedback.getId(),
                notification.getIsRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.MICROS);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new InvalidFilterException(
                    "Page must be greater than or equal to 0"
            );
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidFilterException("Size must be between 1 and 100");
        }
    }
}
