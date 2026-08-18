package com.sdt.feedback.service;

import com.sdt.feedback.exception.AttachmentFileTooLargeException;
import com.sdt.feedback.exception.InvalidAttachmentException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class AttachmentFileValidator {

    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    public ValidatedAttachment validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAttachmentException("Attachment file must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AttachmentFileTooLargeException(
                    "Attachment file must not exceed 5 MB"
            );
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new InvalidAttachmentException(
                    "Unable to read attachment file",
                    exception
            );
        }

        DetectedImageType detectedType = detectType(content);
        return new ValidatedAttachment(
                originalFilename,
                content,
                detectedType.contentType,
                detectedType.extension
        );
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            throw new InvalidAttachmentException("Attachment filename is required");
        }

        String normalized = filename.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        StringBuilder sanitized = new StringBuilder(normalized.length());
        normalized.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .forEach(sanitized::appendCodePoint);

        String safeFilename = sanitized.toString().trim();
        if (safeFilename.isEmpty() || safeFilename.equals(".") || safeFilename.equals("..")) {
            throw new InvalidAttachmentException("Attachment filename is invalid");
        }
        if (safeFilename.length() > 255) {
            throw new InvalidAttachmentException(
                    "Attachment filename must not exceed 255 characters"
            );
        }
        return safeFilename;
    }

    private DetectedImageType detectType(byte[] content) {
        if (isJpeg(content)) {
            return new DetectedImageType("image/jpeg", "jpg");
        }
        if (isPng(content)) {
            return new DetectedImageType("image/png", "png");
        }
        if (isWebp(content)) {
            return new DetectedImageType("image/webp", "webp");
        }
        throw new InvalidAttachmentException(
                "Attachment must be a valid JPEG, PNG, or WebP image"
        );
    }

    private boolean isJpeg(byte[] content) {
        return content.length >= 3
                && unsigned(content[0]) == 0xFF
                && unsigned(content[1]) == 0xD8
                && unsigned(content[2]) == 0xFF;
    }

    private boolean isPng(byte[] content) {
        int[] signature = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (content.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if (unsigned(content[index]) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] content) {
        return content.length >= 12
                && matchesAscii(content, 0, "RIFF")
                && matchesAscii(content, 8, "WEBP");
    }

    private boolean matchesAscii(byte[] content, int offset, String expected) {
        for (int index = 0; index < expected.length(); index++) {
            if (unsigned(content[offset + index]) != expected.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private record DetectedImageType(String contentType, String extension) {
    }

    public record ValidatedAttachment(
            String originalFilename,
            byte[] content,
            String contentType,
            String extension
    ) {
    }
}
