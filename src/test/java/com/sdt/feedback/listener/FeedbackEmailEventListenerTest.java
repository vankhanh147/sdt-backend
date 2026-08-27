package com.sdt.feedback.listener;

import com.sdt.feedback.event.FeedbackCreatedEvent;
import com.sdt.feedback.event.FeedbackResolvedEvent;
import com.sdt.feedback.service.EmailAddressValidator;
import com.sdt.feedback.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedbackEmailEventListenerTest {

    private final EmailService emailService = mock(EmailService.class);
    private final EmailAddressValidator validator = mock(EmailAddressValidator.class);
    private final FeedbackEmailEventListener listener =
            new FeedbackEmailEventListener(emailService, validator);

    @Test
    void sendsCreatedEmailWhenEnabledAndValid() {
        FeedbackCreatedEvent event = new FeedbackCreatedEvent(
                UUID.randomUUID(), "User", "user@example.com", "Title"
        );
        when(emailService.isEnabled()).thenReturn(true);
        when(validator.isValidEmail(event.authorContact())).thenReturn(true);

        listener.handleFeedbackCreated(event);

        verify(emailService).sendFeedbackReceived(event);
    }

    @Test
    void skipsWhenDisabledOrInvalid() {
        FeedbackCreatedEvent disabled = new FeedbackCreatedEvent(
                UUID.randomUUID(), null, "user@example.com", null
        );
        when(emailService.isEnabled()).thenReturn(false);
        listener.handleFeedbackCreated(disabled);
        verify(emailService, never()).sendFeedbackReceived(disabled);

        FeedbackResolvedEvent invalid = new FeedbackResolvedEvent(
                UUID.randomUUID(), null, "0901234567", null
        );
        when(emailService.isEnabled()).thenReturn(true);
        when(validator.isValidEmail(invalid.authorContact())).thenReturn(false);
        listener.handleFeedbackResolved(invalid);
        verify(emailService, never()).sendFeedbackResolved(invalid);
    }

    @Test
    void swallowsMailFailure() {
        FeedbackResolvedEvent event = new FeedbackResolvedEvent(
                UUID.randomUUID(), "User", "user@example.com", "Title"
        );
        when(emailService.isEnabled()).thenReturn(true);
        when(validator.isValidEmail(event.authorContact())).thenReturn(true);
        doThrow(new MailSendException("test failure"))
                .when(emailService).sendFeedbackResolved(event);

        listener.handleFeedbackResolved(event);

        verify(emailService).sendFeedbackResolved(event);
    }
}
