package com.sdt.feedback.service;

import com.sdt.feedback.client.SupabaseStorageClient;
import com.sdt.feedback.dto.request.FeedbackCreateRequest;
import com.sdt.feedback.dto.request.FeedbackUpdateRequest;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.RawFeedback;
import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.event.FeedbackCreatedEvent;
import com.sdt.feedback.event.FeedbackResolvedEvent;
import com.sdt.feedback.mapper.FeedbackCreateMapper;
import com.sdt.feedback.mapper.FeedbackMapper;
import com.sdt.feedback.repository.AnalysisResultRepository;
import com.sdt.feedback.repository.FeedbackAttachmentRepository;
import com.sdt.feedback.repository.FeedbackRepository;
import com.sdt.feedback.repository.RawFeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedbackEmailEventPublishingTest {

    @Test
    void createPublishesCreatedEvent() {
        RawFeedbackRepository rawRepository = mock(RawFeedbackRepository.class);
        FeedbackRepository feedbackRepository = mock(FeedbackRepository.class);
        FeedbackCreateMapper mapper = mock(FeedbackCreateMapper.class);
        NotificationService notificationService = mock(NotificationService.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        FeedbackCreateService service = new FeedbackCreateService(
                rawRepository, feedbackRepository, mapper, notificationService, publisher
        );
        RawFeedback rawFeedback = new RawFeedback();
        Feedback feedback = feedback(FeedbackStatus.PENDING_ANALYSIS);
        when(mapper.toRawFeedback(any())).thenReturn(rawFeedback);
        when(mapper.toFeedback(any())).thenReturn(feedback);
        when(rawRepository.save(rawFeedback)).thenReturn(rawFeedback);
        when(feedbackRepository.saveAndFlush(feedback)).thenReturn(feedback);

        service.create(new FeedbackCreateRequest(
                "Title", "Content", "User", "user@example.com",
                null, null, null
        ));

        verify(publisher).publishEvent(any(FeedbackCreatedEvent.class));
    }

    @Test
    void publishesResolvedEventOnlyForRealTransition() {
        verifyTransitionPublishes(FeedbackStatus.IN_PROGRESS, FeedbackStatus.RESOLVED, true);
        verifyTransitionPublishes(FeedbackStatus.RESOLVED, FeedbackStatus.RESOLVED, false);
        verifyTransitionPublishes(FeedbackStatus.RESOLVED, FeedbackStatus.IN_PROGRESS, false);
    }

    private void verifyTransitionPublishes(
            FeedbackStatus previous,
            FeedbackStatus requested,
            boolean expected
    ) {
        FeedbackRepository feedbackRepository = mock(FeedbackRepository.class);
        AnalysisResultRepository analysisRepository = mock(AnalysisResultRepository.class);
        FeedbackMapper mapper = mock(FeedbackMapper.class);
        FeedbackAttachmentRepository attachmentRepository =
                mock(FeedbackAttachmentRepository.class);
        SupabaseStorageClient storageClient = mock(SupabaseStorageClient.class);
        NotificationService notificationService = mock(NotificationService.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        FeedbackCommandService service = new FeedbackCommandService(
                feedbackRepository,
                analysisRepository,
                mapper,
                attachmentRepository,
                storageClient,
                notificationService,
                publisher
        );
        Feedback feedback = feedback(previous);
        feedback.setRawFeedback(new RawFeedback());
        when(feedbackRepository.findDetailById(feedback.getId()))
                .thenReturn(Optional.of(feedback));
        doAnswer(invocation -> {
            FeedbackUpdateRequest request = invocation.getArgument(0);
            Feedback target = invocation.getArgument(1);
            if (request.status() != null) {
                target.setStatus(request.status());
            }
            return null;
        }).when(mapper).updateEntity(any(), any());
        when(feedbackRepository.saveAndFlush(feedback)).thenReturn(feedback);
        when(analysisRepository.findByFeedback_IdOrderByCreatedAtDesc(feedback.getId()))
                .thenReturn(List.of());
        when(mapper.toAnalysisResultResponses(List.of())).thenReturn(List.of());

        service.updateFeedback(
                feedback.getId(),
                new FeedbackUpdateRequest(null, null, null, null, null, null, requested)
        );

        if (expected) {
            verify(publisher).publishEvent(any(FeedbackResolvedEvent.class));
        } else {
            verify(publisher, never()).publishEvent(any(FeedbackResolvedEvent.class));
        }
    }

    private Feedback feedback(FeedbackStatus status) {
        Feedback feedback = new Feedback();
        feedback.setId(UUID.randomUUID());
        feedback.setTitle("Title");
        feedback.setAuthorName("User");
        feedback.setAuthorContact("user@example.com");
        feedback.setStatus(status);
        return feedback;
    }
}
