package com.board.support;

import com.board.domain.token.service.TokenService;
import com.board.global.security.config.SecurityConfig;
import com.board.support.config.RestDocsConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

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

    protected Claims mockClaims() {
        return Jwts.claims().subject("yoon1234")
                .add("nickname", "yoonkun")
                .add("authority", "ROLE_MEMBER")
                .build();
    }

    protected FieldDescriptor[] commonSuccessResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("message").type(STRING).description("요청 성공/실패 여부"),
                fieldWithPath("status").type(NUMBER).description("Http 상태 코드"),
                subsectionWithPath("result").description("요청 결과 데이터"),
        };
    }

    protected FieldDescriptor[] commonErrorResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("message").type(STRING).description("요청 성공/실패 여부"),
                fieldWithPath("status").type(NUMBER).description("Http 상태 코드"),
                fieldWithPath("result").type(OBJECT).description("에러 결과 데이터"),
                fieldWithPath("result.timeStamp").type(STRING).description("에러 발생 시간"),
                fieldWithPath("result.path").type(STRING).description("요청 api 경로"),
                fieldWithPath("result.error.code").type(STRING).description("에러 코드"),
                fieldWithPath("result.error.message").type(STRING).description("에러 메시지"),
                subsectionWithPath("result.error.fieldErrors").type(ARRAY).description("유효성 검증 에러 필드 목록"),
        };
    }

}
