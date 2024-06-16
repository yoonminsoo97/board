package com.board.support;

import com.board.domain.token.service.TokenService;
import com.board.global.common.config.ObjectMapperConfig;
import com.board.global.security.config.SecurityConfig;
import com.board.global.security.support.JwtManager;
import com.board.support.config.RestDocsConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

@Import({
        SecurityConfig.class,
        RestDocsConfig.class,
        ObjectMapperConfig.class
})
@ExtendWith(RestDocumentationExtension.class)
public abstract class ControllerTest {

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

    @MockBean
    protected JwtManager jwtManager;

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

    protected FieldDescriptor[] commonSuccessResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("status").type(STRING).description("API 요청 성공/실패 상태"),
                subsectionWithPath("data").type(OBJECT).description("응답 데이터").optional()
        };
    }

    protected FieldDescriptor[] commonErrorResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("status").type(STRING).description("API 요청 성공/실패 상태"),
                subsectionWithPath("error").type(OBJECT).description("에러 데이터"),
                fieldWithPath("error.code").type(STRING).description("에러 코드"),
                fieldWithPath("error.message").type(STRING).description("에러 메시지"),
                subsectionWithPath("error.fields").type(ARRAY).description("유효성 에러 필드"),
        };
    }

}
