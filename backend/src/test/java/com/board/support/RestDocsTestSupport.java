package com.board.support;

import com.board.domain.token.service.TokenService;
import com.board.global.security.config.SecurityConfig;
import com.board.support.config.RestDocsConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Import({SecurityConfig.class, RestDocsConfig.class})
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTestSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @MockBean
    protected UserDetailsService userDetailsService;

    @MockBean
    protected TokenService tokenService;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token.expire}")
    private long accessTokenExpire;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocs)
                .build();
    }

    protected String createAccessToken() {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + accessTokenExpire);
        return Jwts.builder()
                .subject("yoon1234")
                .claim("nickname", "yoonkun")
                .claim("authority", "ROLE_MEMBER")
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .issuedAt(iat)
                .expiration(exp)
                .compact();
    }

    protected Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
