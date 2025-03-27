package com.backend.domain.auth.controller;

import com.backend.domain.auth.dto.LoginRequest;
import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> memberLogin(@RequestBody @Valid LoginRequest loginRequest) {
        TokenResponse tokenResponse = authService.memberLogin(loginRequest);
        return ResponseEntity.ok().body(tokenResponse);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/logout")
    public ResponseEntity<Void> memberLogout(HttpServletRequest request, @AuthenticationPrincipal String username) {
        String accessToken = extractToken(request);
        authService.memberLogout(accessToken, username);
        return ResponseEntity.ok().build();
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

}
