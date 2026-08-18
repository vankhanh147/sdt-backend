package com.sdt.feedback.exception;

public class AttachmentLimitExceededException extends RuntimeException {

    public AttachmentLimitExceededException(String message) {
        super(message);
    }
}
