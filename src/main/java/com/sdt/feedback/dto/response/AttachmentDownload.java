package com.sdt.feedback.dto.response;

public record AttachmentDownload(
        byte[] content,
        String contentType,
        String originalFilename
) {
}
