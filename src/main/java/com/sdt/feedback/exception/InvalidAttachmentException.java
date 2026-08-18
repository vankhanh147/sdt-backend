package com.sdt.feedback.exception;

public class InvalidAttachmentException extends RuntimeException {

    public InvalidAttachmentException(String message) {
        super(message);
    }

    public InvalidAttachmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
