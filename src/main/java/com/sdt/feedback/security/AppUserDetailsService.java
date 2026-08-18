package com.sdt.feedback.security;

import com.sdt.feedback.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private static final String AUTHENTICATION_FAILED = "Invalid username or password";

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        String normalizedUsername = username == null ? "" : username.trim();
        return appUserRepository.findByNormalizedUsername(normalizedUsername)
                .map(AppUserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException(AUTHENTICATION_FAILED));
    }
}
