package com.sdt.feedback.security;

import org.springframework.security.core.AuthenticationException;

public class InactiveAccountException extends AuthenticationException {

    public InactiveAccountException() {
        super("Authentication failed");
    }
}
