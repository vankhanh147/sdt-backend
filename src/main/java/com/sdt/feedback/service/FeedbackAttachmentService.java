package com.sdt.feedback.service;

import com.sdt.feedback.client.SupabaseStorageClient;
import com.sdt.feedback.dto.response.AttachmentDownload;
import com.sdt.feedback.dto.response.FeedbackAttachmentResponse;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.FeedbackAttachment;
import com.sdt.feedback.exception.AttachmentLimitExceededException;
import com.sdt.feedback.exception.AttachmentNotFoundException;
import com.sdt.feedback.exception.ResourceNotFoundException;
import com.sdt.feedback.exception.StorageOperationException;
import com.sdt.feedback.repository.FeedbackAttachmentRepository;
import com.sdt.feedback.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class FeedbackAttachmentService {

    public static final int MAX_ATTACHMENTS_PER_FEEDBACK = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(
            FeedbackAttachmentService.class
    );

    private final FeedbackRepository feedbackRepository;
    private final FeedbackAttachmentRepository attachmentRepository;
    private final AttachmentFileValidator fileValidator;
    private final SupabaseStorageClient storageClient;

    public FeedbackAttachmentService(
            FeedbackRepository feedbackRepository,
            FeedbackAttachmentRepository attachmentRepository,
            AttachmentFileValidator fileValidator,
            SupabaseStorageClient storageClient
    ) {
        this.feedbackRepository = feedbackRepository;
        this.attachmentRepository = attachmentRepository;
        this.fileValidator = fileValidator;
        this.storageClient = storageClient;
    }

    @Transactional
    public FeedbackAttachmentResponse upload(UUID feedbackId, MultipartFile file) {
        Feedback feedback = feedbackRepository.findByIdForAttachmentUpdate(feedbackId)
                .orElseThrow(() -> feedbackNotFound(feedbackId));
        if (attachmentRepository.countByFeedback_Id(feedbackId)
                >= MAX_ATTACHMENTS_PER_FEEDBACK) {
            throw new AttachmentLimitExceededException(
                    "A feedback may have at most 5 attachments"
            );
        }

        AttachmentFileValidator.ValidatedAttachment validated = fileValidator
                .validate(file);
        UUID attachmentId = UUID.randomUUID();
        String storagePath = "feedback/" + feedbackId + "/"
                + attachmentId + "." + validated.extension();
        boolean uploaded = false;

        try {
            storageClient.upload(
                    storagePath,
                    validated.content(),
                    validated.contentType()
            );
            uploaded = true;

            FeedbackAttachment attachment = new FeedbackAttachment();
            attachment.setId(attachmentId);
            attachment.setFeedback(feedback);
            attachment.setOriginalFilename(validated.originalFilename());
            attachment.setStoragePath(storagePath);
            attachment.setContentType(validated.contentType());
            attachment.setFileSize((long) validated.content().length);

            return toResponse(attachmentRepository.saveAndFlush(attachment));
        } catch (RuntimeException exception) {
            if (uploaded) {
                compensateUpload(storagePath, exception);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<FeedbackAttachmentResponse> list(UUID feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw feedbackNotFound(feedbackId);
        }
        return attachmentRepository
                .findByFeedback_IdOrderByCreatedAtAscIdAsc(feedbackId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttachmentDownload download(UUID feedbackId, UUID attachmentId) {
        FeedbackAttachment attachment = findAttachment(feedbackId, attachmentId);
        byte[] content = storageClient.download(attachment.getStoragePath());
        return new AttachmentDownload(
                content,
                attachment.getContentType(),
                attachment.getOriginalFilename()
        );
    }

    @Transactional
    public void delete(UUID feedbackId, UUID attachmentId) {
        FeedbackAttachment attachment = findAttachment(feedbackId, attachmentId);
        storageClient.delete(attachment.getStoragePath());
        attachmentRepository.delete(attachment);
        attachmentRepository.flush();
    }

    private FeedbackAttachment findAttachment(UUID feedbackId, UUID attachmentId) {
        return attachmentRepository.findByIdAndFeedback_Id(attachmentId, feedbackId)
                .orElseThrow(() -> new AttachmentNotFoundException(
                        "Attachment not found for the specified feedback"
                ));
    }

    private FeedbackAttachmentResponse toResponse(FeedbackAttachment attachment) {
        UUID feedbackId = attachment.getFeedback().getId();
        return new FeedbackAttachmentResponse(
                attachment.getId(),
                feedbackId,
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getCreatedAt(),
                "/api/feedback/" + feedbackId + "/attachments/"
                        + attachment.getId() + "/download"
        );
    }

    private ResourceNotFoundException feedbackNotFound(UUID feedbackId) {
        return new ResourceNotFoundException(
                "Feedback not found with id=" + feedbackId
        );
    }

    private void compensateUpload(String storagePath, RuntimeException original) {
        try {
            storageClient.delete(storagePath);
        } catch (StorageOperationException compensationFailure) {
            LOGGER.error(
                    "Attachment metadata save failed and storage compensation failed for path {}",
                    storagePath,
                    compensationFailure
            );
            original.addSuppressed(compensationFailure);
        }
    }
}
