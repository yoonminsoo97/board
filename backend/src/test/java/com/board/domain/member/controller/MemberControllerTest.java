package com.board.domain.member.controller;

import com.board.domain.member.dto.MemberNicknameRequest;
import com.board.domain.member.dto.MemberPasswordRequest;
import com.board.domain.member.dto.MemberProfileResponse;
import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.service.MemberService;
import com.board.domain.post.dto.PostListResponse;
import com.board.global.security.exception.ExpiredTokenException;
import com.board.global.security.exception.InvalidTokenException;
import com.board.global.security.dto.LoginMember;

import com.board.support.ControllerTest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.formParameters;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
class MemberControllerTest extends ControllerTest {

    @MockBean
    private MemberService memberService;

    @Nested
    @DisplayName("닉네임 중복 확인 요청")
    class MemberNicknameExistsTest {

        @Test
        @DisplayName("닉네임이 중복되지 않는다")
        void memberNicknameExists() throws Exception {
            willDoNothing().given(memberService).memberNicknameExists(anyString());

            mockMvc.perform(get("/api/members/nickname/{nickname}", "yoonkun"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("nickname").description("닉네임")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("닉네임이 중복되면 예외가 발생한다")
        void memberNicknameExistsDuplicateNickname() throws Exception {
            willThrow(new DuplicateNicknameException()).given(memberService).memberNicknameExists(anyString());

            mockMvc.perform(get("/api/members/nickname/{nickname}", "yoonkun"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E409001"))
                    .andExpect(jsonPath("$.error.message").value("사용 중인 닉네임입니다."))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("nickname").description("닉네임")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));

        }

    }

    @Nested
    @DisplayName("아이디 중복 확인 요청")
    class MemberUsernameExistsTest {

        @Test
        @DisplayName("아이디가 중복되지 않는다")
        void memberUsernameExists() throws Exception {
            willDoNothing().given(memberService).memberUsernameExists(anyString());

            mockMvc.perform(get("/api/members/username/{username}", "yoon1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("username").description("아이디")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("아이디가 중복되면 예외가 발생한다")
        void memberUsernameExistsDuplicateUsername() throws Exception {
            willThrow(new DuplicateUsernameException()).given(memberService).memberUsernameExists(anyString());

            mockMvc.perform(get("/api/members/username/{username}", "yoon1234"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E409002"))
                    .andExpect(jsonPath("$.error.message").value("사용 중인 아이디입니다."))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("username").description("아이디")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("회원가입 요청")
    class MemberSignupTest {

        @Test
        @DisplayName("회원가입을 한다")
        void memberSignup() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("닉네임이 비어있으면 예외가 발생한다")
        void memberSignupInvalidNicknameValue() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("nickname"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("닉네임을 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));

        }

        @Test
        @DisplayName("아이디가 비어있으면 예외가 발생한다")
        void memberSignupInvalidUsernameValue() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("username"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("아이디를 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("비밀번호가 비어있으면 예외가 발생한다")
        void memberSignupInvalidPasswordValue() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("")
                    .passwordConfirm("12345678")
                    .build();

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("password"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("비밀번호를 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("비밀번호 확인이 비어있으면 예외가 발생한다")
        void memberSignupInvalidPasswordConfirmValue() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("")
                    .build();

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("passwordConfirm"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("비밀번호를 한 번 더 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("닉네임이 중복되면 예외가 발생한다")
        void memberSignupDuplicateNickname() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            willThrow(new DuplicateNicknameException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E409001"))
                    .andExpect(jsonPath("$.error.message").value("사용 중인 닉네임입니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("아이디가 중복되면 예외가 발생한다")
        void memberSignupDuplicateUsername() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            willThrow(new DuplicateUsernameException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E409002"))
                    .andExpect(jsonPath("$.error.message").value("사용 중인 아이디입니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 예외가 발생한다")
        void memberSignupPasswordAndPasswordConfirmMismatch() throws Exception {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("87654321")
                    .build();

            willThrow(new PasswordMismatchException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

            mockMvc.perform(post("/api/members/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberSignupRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400002"))
                    .andExpect(jsonPath("$.error.message").value("비밀번호가 일치하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("닉네임"),
                                    fieldWithPath("username").type(STRING).description("아이디"),
                                    fieldWithPath("password").type(STRING).description("비밀번호"),
                                    fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("로그인 요청")
    class MemberLoginTest {

        @Test
        @DisplayName("로그인을 한다")
        void memberLogin() throws Exception {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            LoginMember loginMember = new LoginMember(member);

            given(userDetailsService.loadUserByUsername(anyString())).willReturn(loginMember);
            given(jwtManager.createAccessToken(any(LoginMember.class))).willReturn("access-token");
            given(jwtManager.createRefreshToken()).willReturn("refresh-token");
            willDoNothing().given(tokenService).saveToken(anyString(), any(Member.class));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "yoon1234")
                            .param("password", "12345678")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andDo(restDocs.document(
                            formParameters(
                                    parameterWithName("username").description("아이디"),
                                    parameterWithName("password").description("비밀번호")
                            ),
                            responseFields(
                                    commonSuccessResponse())
                                    .and(
                                            fieldWithPath("data.accessToken").type(STRING).description("액세스 토큰"),
                                            fieldWithPath("data.refreshToken").type(STRING).description("리프레시 토큰")
                                    )
                    ));

        }

        @Test
        @DisplayName("아이디 또는 비밀번호가 일치하지 않으면 예외가 발생한다")
        void memberLoginBadCredentials() throws Exception {
            willThrow(UsernameNotFoundException.class).given(userDetailsService).loadUserByUsername(anyString());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "yoon1234")
                            .param("password", "12345678")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401001"))
                    .andExpect(jsonPath("$.error.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            formParameters(
                                    parameterWithName("username").description("아이디"),
                                    parameterWithName("password").description("비밀번호")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("로그아웃 요청")
    class MemberLogoutTest {

        @Test
        @DisplayName("로그아웃을 한다")
        void memberLogout() throws Exception {
            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(tokenService).deleteToken(anyLong());

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));

        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void memberLogoutInvalidAccessToken() throws Exception {
            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer invalid-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void memberLogoutExpiredAccessToken() throws Exception {
            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer invalid-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("회원 상세정보 조회 요청")
    class MemberProfileTest {

        @Test
        @DisplayName("회원 상세정보를 조회한다")
        void memberProfile() throws Exception {
            MemberProfileResponse memberProfileResponse = MemberProfileResponse.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            given(memberService.memberProfile(anyLong())).willReturn(memberProfileResponse);

            mockMvc.perform(get("/api/members/me")
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonSuccessResponse())
                                    .and(
                                            fieldWithPath("data.nickname").type(STRING).description("닉네임"),
                                            fieldWithPath("data.username").type(STRING).description("아이디")
                                    )
                    ));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
        void memberProfileNotFoundMember() throws Exception {
            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundMemberException()).given(memberService).memberProfile(anyLong());

            mockMvc.perform(get("/api/members/me")
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E404001"))
                    .andExpect(jsonPath("$.error.message").value("회원을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void memberProfileInvalidAccessToken() throws Exception {
            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(get("/api/members/me")
                            .header("Authorization", "Bearer invalid-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void memberProfileExpiredAccessToken() throws Exception {
            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(get("/api/members/me")
                            .header("Authorization", "Bearer expired-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("회원이 작성한 게시글 목록 조회 요청")
    class MemberPostListTest {

        @Test
        @DisplayName("회원이 작성한 게시글 목록을 조회한다")
        void memberPostList() throws Exception {
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(List.of(
                            PostListResponse.PostItem.builder()
                                    .postId(1L)
                                    .title("title")
                                    .writer("writer")
                                    .commentCount(0)
                                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                                    .build()
                    ))
                    .page(1)
                    .totalPages(1)
                    .totalElements(1)
                    .first(true)
                    .last(true)
                    .prev(false)
                    .next(false)
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            given(memberService.memberPostList(anyInt(), anyLong())).willReturn(postListResponse);

            mockMvc.perform(get("/api/members/me/posts")
                            .header("Authorization", "Bearer access-token")
                            .param("page", "1")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.posts[0].postId").value(1))
                    .andExpect(jsonPath("$.data.posts[0].title").value("title"))
                    .andExpect(jsonPath("$.data.posts[0].writer").value("writer"))
                    .andExpect(jsonPath("$.data.posts[0].commentCount").value(0))
                    .andExpect(jsonPath("$.data.posts[0].createdAt").value("2024-06-17T00:00:00"))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.data.prev").value(false))
                    .andExpect(jsonPath("$.data.next").value(false))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            queryParameters(
                                    parameterWithName("page").description("페이지 번호")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void memberPostListInvalidAccessToken() throws Exception {
            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(get("/api/members/me/posts")
                            .header("Authorization", "Bearer invalid-token")
                            .param("page", "1")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andDo(restDocs.document(
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void memberPostListExpiredAccessToken() throws Exception {
            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(get("/api/members/me/posts")
                            .header("Authorization", "Bearer expired-token")
                            .param("page", "1")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andDo(restDocs.document(
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("회원 닉네임 변경 요청")
    class MemberNicknameChangeTest {

        @Test
        @DisplayName("닉네임을 변경한다")
        void memberNicknameChange() throws Exception {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("newNickname")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(memberService).memberNicknameChange(any(MemberNicknameRequest.class), anyLong());

            mockMvc.perform(put("/api/members/me/nickname")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberNicknameRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("변경할 닉네임")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("닉네임이 비어있으면 예외가 발생한다")
        void memberNicknameChangeInvalidNicknameValue() throws Exception {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(put("/api/members/me/nickname")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberNicknameRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("nickname"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("닉네임을 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("변경할 닉네임")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
        void memberNicknameChangeNotFoundMember() throws Exception {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("newNickname")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundMemberException()).given(memberService).memberNicknameChange(any(MemberNicknameRequest.class), anyLong());

            mockMvc.perform(put("/api/members/me/nickname")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberNicknameRequest))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E404001"))
                    .andExpect(jsonPath("$.error.message").value("회원을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("변경할 닉네임")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("닉네임이 중복되면 예외가 발생한다")
        void memberNicknameChangeDuplicateNickname() throws Exception {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("newNickname")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new DuplicateNicknameException()).given(memberService).memberNicknameChange(any(MemberNicknameRequest.class), anyLong());

            mockMvc.perform(put("/api/members/me/nickname")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberNicknameRequest))
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E409001"))
                    .andExpect(jsonPath("$.error.message").value("사용 중인 닉네임입니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("변경할 닉네임")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void memberNicknameInvalidAccessToken() throws Exception {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("newNickname")
                    .build();

            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/members/me/nickname")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberNicknameRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("변경할 닉네임")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void memberNicknameExpiredAccessToken() throws Exception {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("newNickname")
                    .build();

            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/members/me/nickname")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberNicknameRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("nickname").type(STRING).description("변경할 닉네임")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("회원 비밀번호 변경 요청")
    class MemberPasswordChangeTest {

        @Test
        @DisplayName("비밀번호를 변경한다")
        void memberPasswordChange() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(memberService).memberPasswordChange(any(MemberPasswordRequest.class), anyLong());

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("현재 비밀번호가 비어있으면 예외가 발생한다")
        void memberPasswordInvalidCurPassword() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("curPassword"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("현재 비밀번호를 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("새 비밀번호가 비어있으면 예외가 발생한다")
        void memberPasswordInvalidNewPassword() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("")
                    .newPasswordConfirm("87654321")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("newPassword"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("새로운 비밀번호를 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("새 비밀번호 확인이 비어있으면 예외가 발생한다")
        void memberPasswordInvalidNewPasswordConfirm() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("newPasswordConfirm"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("새로운 비밀번호를 한 번 더 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
        void memberPasswordChangeNotFoundMember() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundMemberException()).given(memberService).memberPasswordChange(any(MemberPasswordRequest.class), anyLong());

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E404001"))
                    .andExpect(jsonPath("$.error.message").value("회원을 찾을 수 없습니다."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("현재 사용 중인 비밀번호가 일치하지 않으면 예외가 발생한다")
        void memberPasswordChangeCurPasswordMismatch() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new PasswordMismatchException()).given(memberService).memberPasswordChange(any(MemberPasswordRequest.class), anyLong());

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400002"))
                    .andExpect(jsonPath("$.error.message").value("비밀번호가 일치하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("새 비밀번호와 새 비밀번호 확인이 일치하지 않으면 예외가 발생하다")
        void memberPassowrdChangeNewPasswordAndNewPasswordConfirmMismatch() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("97654321")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new PasswordMismatchException()).given(memberService).memberPasswordChange(any(MemberPasswordRequest.class), anyLong());

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400002"))
                    .andExpect(jsonPath("$.error.message").value("비밀번호가 일치하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void memberPasswordChangeInvalidAccessToken() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer invalid-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));

        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void memberPasswordChangeExpiredAccessToken() throws Exception {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/members/me/password")
                            .header("Authorization", "Bearer invalid-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(memberPasswordRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("curPassword").type(STRING).description("현재 사용 중인 비밀번호"),
                                    fieldWithPath("newPassword").type(STRING).description("변경할 비밀번호"),
                                    fieldWithPath("newPasswordConfirm").type(STRING).description("변경할 비밀번호 확인")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

}