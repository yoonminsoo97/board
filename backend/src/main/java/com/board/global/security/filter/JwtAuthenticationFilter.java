package com.board.global.security.filter;

import com.board.domain.token.service.TokenService;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            Claims payload = tokenService.tokenPayload(token);
            Authentication authentication = createAuthentication(payload);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            authenticationEntryPoint.commence(request, response, e);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer")) {
            return header.substring("Bearer".length()).trim();
        }
        return null;
    }

    private Authentication createAuthentication(Claims payload) {
        String username = payload.getSubject();
        String authority = payload.get("authority", String.class);
        return UsernamePasswordAuthenticationToken.authenticated(username, null, createAuthorityList(authority));
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return ExcludeRequest.isExcludeRequest(request.getMethod(), request.getRequestURI());
    }

    @Getter
    private enum ExcludeRequest {

        REST_DOCS("GET", "/docs/board.html"),
        MEMBER_NICKNAME_EXISTS("GET", "/api/members/nickname/*"),
        MEMBER_USERNAME_EXISTS("GET", "/api/members/username/*"),
        MEMBER_SIGNUP("POST", "/api/members/signup"),
        MEMBER_LOGIN("POST", "/api/members/login"),
        POST_DETAIL("GET", "/api/posts/*"),
        POST_LIST("GET", "/api/posts"),
        POST_LIST_SEARCH("GET", "/api/posts/search"),
        COMMENT_LIST("GET", "/api/posts/*/comments/page/*"),
        REISSUE_ACCESS_TOKEN("POST", "/api/tokens/reissue");

        private final String method;
        private final String pattern;

        ExcludeRequest(String method, String pattern) {
            this.method = method;
            this.pattern = pattern;
        }

        private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

        public static boolean isExcludeRequest(String requestMethod, String requestURI) {
            return Arrays.stream(ExcludeRequest.values())
                    .anyMatch(e -> e.method.equals(requestMethod) && ANT_PATH_MATCHER.match(e.pattern, requestURI));
        }

    }

}
