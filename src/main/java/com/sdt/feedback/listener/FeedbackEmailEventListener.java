package com.sdt.feedback.listener;

import com.sdt.feedback.event.FeedbackCreatedEvent;
import com.sdt.feedback.event.FeedbackResolvedEvent;
import com.sdt.feedback.service.EmailAddressValidator;
import com.sdt.feedback.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FeedbackEmailEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            FeedbackEmailEventListener.class
    );

    private final EmailService emailService;
    private final EmailAddressValidator emailAddressValidator;

    public FeedbackEmailEventListener(
            EmailService emailService,
            EmailAddressValidator emailAddressValidator
    ) {
        this.emailService = emailService;
        this.emailAddressValidator = emailAddressValidator;
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedbackCreated(FeedbackCreatedEvent event) {
        handleCreated(event);
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedbackResolved(FeedbackResolvedEvent event) {
        handleResolved(event);
    }

    private void handleCreated(FeedbackCreatedEvent event) {
        if (!shouldSend("CREATED", event.feedbackId(), event.authorContact())) {
            return;
        }
        try {
            emailService.sendFeedbackReceived(event);
            logSuccess("CREATED", event.feedbackId(), event.authorContact());
        } catch (MailException exception) {
            logFailure("CREATED", event.feedbackId(), event.authorContact(), exception);
        } catch (Exception exception) {
            logFailure("CREATED", event.feedbackId(), event.authorContact(), exception);
        }
    }

    private void handleResolved(FeedbackResolvedEvent event) {
        if (!shouldSend("RESOLVED", event.feedbackId(), event.authorContact())) {
            return;
        }
        try {
            emailService.sendFeedbackResolved(event);
            logSuccess("RESOLVED", event.feedbackId(), event.authorContact());
        } catch (MailException exception) {
            logFailure("RESOLVED", event.feedbackId(), event.authorContact(), exception);
        } catch (Exception exception) {
            logFailure("RESOLVED", event.feedbackId(), event.authorContact(), exception);
        }
    }

    private boolean shouldSend(String type, Object feedbackId, String recipient) {
        if (!emailService.isEnabled()) {
            LOGGER.debug("Email skipped: type={}, feedbackId={}, reason=disabled",
                    type, feedbackId);
            return false;
        }
        if (!emailAddressValidator.isValidEmail(recipient)) {
            LOGGER.debug("Email skipped: type={}, feedbackId={}, reason=invalid-contact",
                    type, feedbackId);
            return false;
        }
        return true;
    }

    private void logSuccess(String type, Object feedbackId, String recipient) {
        LOGGER.info("Email sent: type={}, feedbackId={}, recipient={}",
                type, feedbackId, mask(recipient));
    }

    private void logFailure(
            String type,
            Object feedbackId,
            String recipient,
            Exception exception
    ) {
        LOGGER.warn(
                "Email failed: type={}, feedbackId={}, recipient={}, error={}",
                type,
                feedbackId,
                mask(recipient),
                exception.getClass().getSimpleName()
        );
    }

    private String mask(String recipient) {
        if (recipient == null) {
            return "<none>";
        }
        String value = recipient.trim();
        int at = value.indexOf('@');
        if (at <= 0 || at == value.length() - 1) {
            return "<invalid>";
        }
        return value.charAt(0) + "***@" + value.substring(at + 1);
    }
}
