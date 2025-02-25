package com.backend.domain.auth.controller;

import com.backend.domain.auth.dto.MemberLoginRequest;
import com.backend.domain.auth.dto.MemberLoginResponse;
import com.backend.domain.auth.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<MemberLoginResponse> memberLogin(@RequestBody @Valid MemberLoginRequest memberLoginRequest) {
        MemberLoginResponse memberLoginResponse = authService.memberLogin(memberLoginRequest);
        return ResponseEntity.ok().body(memberLoginResponse);
    }

}
