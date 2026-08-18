package com.sdt.feedback.controller;

import com.sdt.feedback.dto.response.AttachmentDownload;
import com.sdt.feedback.dto.response.FeedbackAttachmentResponse;
import com.sdt.feedback.service.FeedbackAttachmentService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedback/{feedbackId}/attachments")
public class FeedbackAttachmentController {

    private final FeedbackAttachmentService attachmentService;

    public FeedbackAttachmentController(
            FeedbackAttachmentService attachmentService
    ) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FeedbackAttachmentResponse> upload(
            @PathVariable UUID feedbackId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attachmentService.upload(feedbackId, file));
    }

    @GetMapping
    public ResponseEntity<List<FeedbackAttachmentResponse>> list(
            @PathVariable UUID feedbackId
    ) {
        return ResponseEntity.ok(attachmentService.list(feedbackId));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable UUID feedbackId,
            @PathVariable UUID attachmentId
    ) {
        AttachmentDownload download = attachmentService.download(
                feedbackId,
                attachmentId
        );
        ByteArrayResource resource = new ByteArrayResource(download.content());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.originalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.content().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID feedbackId,
            @PathVariable UUID attachmentId
    ) {
        attachmentService.delete(feedbackId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
