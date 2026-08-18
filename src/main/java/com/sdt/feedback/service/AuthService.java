package com.sdt.feedback.service;

import com.sdt.feedback.dto.request.LoginRequest;
import com.sdt.feedback.dto.response.AuthenticatedUserResponse;
import com.sdt.feedback.dto.response.LoginResponse;
import com.sdt.feedback.security.AppUserPrincipal;
import com.sdt.feedback.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        username,
                        request.password()
                )
        );
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                toResponse(principal)
        );
    }

    public AuthenticatedUserResponse getCurrentUser(AppUserPrincipal principal) {
        return toResponse(principal);
    }

    private AuthenticatedUserResponse toResponse(AppUserPrincipal principal) {
        return new AuthenticatedUserResponse(
                principal.id(),
                principal.getUsername(),
                principal.role()
        );
    }
}
