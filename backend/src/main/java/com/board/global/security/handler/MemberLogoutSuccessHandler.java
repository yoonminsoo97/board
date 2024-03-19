package com.board.global.security.handler;

import com.board.domain.token.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@RequiredArgsConstructor
public class MemberLogoutSuccessHandler implements LogoutSuccessHandler {

    private final TokenService tokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) {
        tokenService.tokenDelete(authentication.getName());
        response.setStatus(HttpStatus.OK.value());
    }

}
