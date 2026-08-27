package com.sdt.feedback.service;

import com.sdt.feedback.event.FeedbackCreatedEvent;
import com.sdt.feedback.event.FeedbackResolvedEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final String RECEIVED_SUBJECT =
            "Phản ánh của bạn đã được tiếp nhận";
    private static final String RESOLVED_SUBJECT =
            "Phản ánh của bạn đã được xử lý";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String from;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled}") boolean enabled,
            @Value("${app.mail.from}") String from
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.from = from;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void sendFeedbackReceived(FeedbackCreatedEvent event) {
        if (!enabled) {
            return;
        }
        send(
                event.authorContact(),
                RECEIVED_SUBJECT,
                receivedBody(event)
        );
    }

    public void sendFeedbackResolved(FeedbackResolvedEvent event) {
        if (!enabled) {
            return;
        }
        send(
                event.authorContact(),
                RESOLVED_SUBJECT,
                resolvedBody(event)
        );
    }

    private void send(String recipient, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException(
                    "Mail sender is unavailable; check SMTP configuration"
            );
        }
        if (from == null || from.isBlank()) {
            throw new IllegalStateException(
                    "MAIL_FROM is required when mail is enabled"
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from.trim());
        message.setTo(recipient.trim());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String receivedBody(FeedbackCreatedEvent event) {
        StringBuilder body = new StringBuilder(greeting(event.authorName()))
                .append("\n\n")
                .append("Phản ánh của bạn đã được hệ thống tiếp nhận thành công.")
                .append("\n\nMã phản ánh: ")
                .append(event.feedbackId());
        appendTitle(body, event.title());
        body.append("\nTrạng thái hiện tại: PENDING_ANALYSIS")
                .append("\n\nChúng tôi sẽ thông báo khi phản ánh được xử lý hoàn tất.");
        return body.toString();
    }

    private String resolvedBody(FeedbackResolvedEvent event) {
        StringBuilder body = new StringBuilder(greeting(event.authorName()))
                .append("\n\n")
                .append("Phản ánh của bạn đã được xử lý hoàn tất.")
                .append("\n\nMã phản ánh: ")
                .append(event.feedbackId());
        appendTitle(body, event.title());
        body.append("\nTrạng thái: RESOLVED")
                .append("\n\nCảm ơn bạn đã gửi phản ánh.");
        return body.toString();
    }

    private String greeting(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            return "Xin chào,";
        }
        return "Xin chào " + singleLine(authorName) + ",";
    }

    private void appendTitle(StringBuilder body, String title) {
        if (title != null && !title.isBlank()) {
            body.append("\nTiêu đề: ").append(singleLine(title));
        }
    }

    private String singleLine(String value) {
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
