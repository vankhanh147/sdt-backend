package com.sdt.feedback.service;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.stereotype.Component;

@Component
public class EmailAddressValidator {

    public boolean isValidEmail(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String candidate = value.trim();
        if (candidate.contains(",") || candidate.contains(";")) {
            return false;
        }
        try {
            InternetAddress address = new InternetAddress(candidate, true);
            address.validate();
            return address.getPersonal() == null
                    && candidate.equals(address.getAddress());
        } catch (AddressException exception) {
            return false;
        }
    }
}
