package com.board.domain.token.controller;

import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.service.TokenService;
import com.board.global.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reIssueAccessToken(@RequestHeader("Authorization") String header) {
        String refreshToken = extractRefreshToken(header);
        TokenResponse tokenResponse = tokenService.reIssueAccessToken(refreshToken);
        return ResponseEntity.ok().body(ApiResponse.success(tokenResponse));
    }

    private String extractRefreshToken(String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer")) {
            return header.substring("Bearer".length()).trim();
        }
        return null;
    }

}
