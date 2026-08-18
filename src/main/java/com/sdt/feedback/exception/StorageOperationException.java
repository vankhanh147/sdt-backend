package com.sdt.feedback.exception;

public class StorageOperationException extends RuntimeException {

    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
