package com.sdt.feedback.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailAddressValidatorTest {

    private final EmailAddressValidator validator = new EmailAddressValidator();

    @Test
    void acceptsSingleNormalEmail() {
        assertTrue(validator.isValidEmail("user@example.com"));
    }

    @Test
    void rejectsNonEmailContacts() {
        assertFalse(validator.isValidEmail(null));
        assertFalse(validator.isValidEmail("  "));
        assertFalse(validator.isValidEmail("0901234567"));
        assertFalse(validator.isValidEmail("user@"));
        assertFalse(validator.isValidEmail("User <user@example.com>"));
        assertFalse(validator.isValidEmail("a@example.com,b@example.com"));
        assertFalse(validator.isValidEmail("a@example.com; b@example.com"));
    }
}
