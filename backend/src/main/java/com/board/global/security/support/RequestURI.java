package com.board.global.security.support;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;

import java.util.Arrays;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

public enum RequestURI {

    REST_API_DOCS(HttpMethod.GET, "/docs/board.html", true),
    MEMBER_NICKNAME_EXISTS(HttpMethod.GET, "/api/members/nickname/*", true),
    MEMBER_USERNAME_EXISTS(HttpMethod.GET, "/api/members/username/*", true),
    MEMBER_SIGNUP(HttpMethod.POST, "/api/members/signup", true),
    MEMBER_LOGIN(HttpMethod.POST, "/api/auth/login", true),
    MEMBER_LOGOUT(HttpMethod.POST, "/api/auth/logout", false),
    MEMBER_PROFILE(HttpMethod.GET, "/api/members/me", false),
    MEMBER_POST_LIST(HttpMethod.GET, "/api/members/me/posts", false),
    MEMBER_COMMENT_LIST(HttpMethod.GET, "/api/members/me/comments", false),
    MEMBER_NICKNAME_CHANGE(HttpMethod.PUT, "/api/members/me/nickname", false),
    MEMBER_PASSWORD_CHANGE(HttpMethod.PUT, "/api/members/me/password", false),
    POST_WRITE(HttpMethod.POST, "/api/posts", false),
    POST_DETAIL(HttpMethod.GET, "/api/posts/*", true),
    POST_LIST(HttpMethod.GET, "/api/posts", true),
    POST_SERACH_LIST(HttpMethod.GET, "/api/posts/search", true),
    POST_MODIFY(HttpMethod.PUT, "/api/posts/*", false),
    POST_DELETE(HttpMethod.DELETE, "/api/posts/*", false),
    COMMENT_WRITE(HttpMethod.POST, "/api/posts/*/comments", false),
    COMMENT_REPLRY_WRITE(HttpMethod.POST, "/api/posts/*/comments/*/replies", false),
    COMMENT_LIST(HttpMethod.GET, "/api/posts/*/comments", true),
    COMMENT_MODIFY(HttpMethod.PUT, "/api/posts/*/comments/*", false),
    COMMENT_DELETE(HttpMethod.DELETE, "/api/posts/*/comments/*", false),
    REISSUE_ACCESS_TOKEN(HttpMethod.POST, "/api/token/reissue", true);

    private final HttpMethod method;
    private final String pattern;
    private final boolean permitAll;

    RequestURI(HttpMethod method, String pattern, boolean permitAll) {
        this.method = method;
        this.pattern = pattern;
        this.permitAll = permitAll;
    }

    public static boolean sholdNotFilter(HttpServletRequest request) {

        return Arrays.stream(RequestURI.values())
                .anyMatch(uri -> uri.permitAll && antMatcher(uri.method, uri.pattern).matches(request));
    }

    public String pattern() {
        return pattern;
    }

}
