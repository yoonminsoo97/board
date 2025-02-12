package com.board.global.security.filter;

import com.board.domain.token.service.TokenService;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = parseToken(request);
            Claims payload = tokenService.getClaims(token);
            Authentication authentication = createAuthentication(payload);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        }
    }

    private String parseToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Authentication createAuthentication(Claims payload) {
        String username = payload.getSubject();
        String authority = payload.get("authority", String.class);
        return UsernamePasswordAuthenticationToken
                .authenticated(username, null, AuthorityUtils.createAuthorityList(authority));
    }

    /**
     * 특정 요청 경로에 대해 필터링 여부를 결정한다.
     *
     * @param request 현재 HTTP 요청 객체
     * @return true면 필터가 동작하지 않으며 false면 필터가 동작한다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return RequestPath.isShouldNotFilter(request);
    }

    @Getter
    private enum RequestPath {

        // 전부 허용
        MEMBER_SIGNUP(HttpMethod.POST, "/api/members/signup", "ALL"),
        MEMBER_LOGIN(HttpMethod.POST, "/api/auth/login", "ALL"),
        POST_DETAIL(HttpMethod.GET, "/api/posts/*", "ALL"),

        // 회원(MEMBER) 권한만 허용
        MEMBER_LOGOUT(HttpMethod.POST, "/api/auth/logout", "MEMBER"),
        POST_WRITE(HttpMethod.POST, "/api/posts/write", "MEMBER"),
        POST_MODIFY(HttpMethod.PUT, "/api/posts/*", "MEMBER"),
        POST_DELETE(HttpMethod.DELETE, "/api/posts/*", "MEMBER");

        private final HttpMethod httpMethod;
        private final String pattern;
        private final String authority;

        RequestPath(HttpMethod httpMethod, String pattern, String authority) {
            this.httpMethod = httpMethod;
            this.pattern = pattern;
            this.authority = authority;
        }

        private static boolean isShouldNotFilter(HttpServletRequest request) {
            return Arrays.stream(RequestPath.values())
                    .anyMatch((requestPath) -> requestPath.authority.equals("ALL") &&
                            antMatcher(requestPath.httpMethod, requestPath.pattern).matches(request));
        }

    }

}
