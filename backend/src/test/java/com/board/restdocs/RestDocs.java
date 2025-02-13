package com.board.restdocs;

import com.board.domain.token.service.TokenService;
import com.board.global.security.config.SecurityConfig;
import com.board.global.security.service.MemberUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@Import({SecurityConfig.class, RestDocsConfig.class})
public abstract class RestDocs {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected RestDocumentationResultHandler restdocs;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected MemberUserDetailsService memberUserDetailsService;

    @MockitoBean
    protected TokenService tokenService;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentationContextProvider) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentationContextProvider))
                .apply(springSecurity())
                .defaultRequest(request(GET, "/").characterEncoding(StandardCharsets.UTF_8))
                .alwaysDo(print())
                .alwaysDo(restdocs) // 모든 API 테스트 케이스에 대한 ascii 문서 생성
                .build();
    }

}
