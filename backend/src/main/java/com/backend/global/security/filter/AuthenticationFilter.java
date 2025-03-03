package com.backend.global.security.filter;

import com.backend.domain.auth.service.TokenService;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        tokenService.validateToken(token);
        Claims claims = tokenService.extractClaim(token);
        Authentication authentication = createAuthentication(claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Authentication createAuthentication(Claims claims) {
        String username = claims.get("username", String.class);
        String authority = claims.get("authority", String.class);
        return UsernamePasswordAuthenticationToken.authenticated(username, null, createAuthorityList(authority));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return RequestPath.isShouldNotFilter(request);
    }

    private enum RequestPath {

        // PERMIT_ALL
        MEMBER_SIGNUP(HttpMethod.POST, "/api/members/signup", Authority.PERMIT_ALL),
        MEMBER_LOGIN(HttpMethod.POST, "/api/auth/login", Authority.PERMIT_ALL),
        POST_DETAIL(HttpMethod.GET, "/api/posts/*", Authority.PERMIT_ALL),
        POST_LIST(HttpMethod.GET, "/api/posts", Authority.PERMIT_ALL),

        // ROLE_MEMBER
        MEMBER_LOGOUT(HttpMethod.POST, "/api/auth/logout", Authority.ROLE_MEMBER),
        POST_WRITE(HttpMethod.POST, "/api/posts/write", Authority.ROLE_MEMBER),
        POST_MODIFY(HttpMethod.PUT, "/api/posts/*", Authority.ROLE_MEMBER),
        POST_DELETE(HttpMethod.DELETE, "/api/posts/*", Authority.ROLE_MEMBER);

        enum Authority {
            PERMIT_ALL, ROLE_MEMBER
        }

        private final HttpMethod httpMethod;
        private final String pattern;
        private final Authority authority;

        RequestPath(HttpMethod httpMethod, String pattern, Authority authority) {
            this.httpMethod = httpMethod;
            this.pattern = pattern;
            this.authority = authority;
        }

        private static boolean isShouldNotFilter(HttpServletRequest request) {
            return Arrays.stream(RequestPath.values())
                    .anyMatch(requestPath -> requestPath.authority.equals(Authority.PERMIT_ALL) &&
                            antMatcher(requestPath.httpMethod, requestPath.pattern).matches(request));
        }

    }

}
