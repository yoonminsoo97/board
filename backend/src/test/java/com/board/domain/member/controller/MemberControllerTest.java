package com.board.domain.member.controller;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.service.MemberService;
import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.exception.InvalidTokenException;
import com.board.domain.token.service.TokenService;
import com.board.global.security.config.SecurityConfig;
import com.board.global.security.dto.AuthPrincipal;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
@Import(SecurityConfig.class)
class MemberControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private MemberService memberService;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token.expire}")
    private long accessTokenExpire;

    @Test
    @DisplayName("닉네임 중복 확인을 한다")
    void memberNicknameExists() throws Exception {
        willDoNothing().given(memberService).memberNicknameExists(anyString());

        mockMvc.perform(get("/api/members/nickname/yoonkun"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 중복 시 예외가 발생한다")
    void memberNicknameExists_duplicateNickname() throws Exception {
        willThrow(new DuplicateNicknameException()).given(memberService).memberNicknameExists(anyString());

        mockMvc.perform(get("/api/members/nickname/yoonkun"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("E409001"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("사용 중인 닉네임입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("아이디 중복 확인을 한다")
    void memberUsernameExists() throws Exception {
        willDoNothing().given(memberService).memberUsernameExists(anyString());

        mockMvc.perform(get("/api/members/username/yoon1234"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andDo(print());
    }

    @Test
    @DisplayName("아이디 중복 시 예외가 발생한다")
    void memberUsernameExists_duplicateUsername() throws Exception {
        willThrow(new DuplicateUsernameException()).given(memberService).memberUsernameExists(anyString());

        mockMvc.perform(get("/api/members/username/yoon1234"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("E409002"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("사용 중인 아이디입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입을 한다")
    void memberSignup() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678", "12345678");

        willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 시 입력값이 잘못되면 예외가 발생한다")
    void memberSignup_invalidInputValue() throws Exception {
        MemberSignupRequest invalidMemberSignupRequest = new MemberSignupRequest("", "", "12345678", "12345678");

        willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMemberSignupRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 일치하지 않으면 예외가 발생한다")
    void memberSignup_passwordMismatch() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678", "12345679");

        willThrow(new PasswordMismatchException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400002"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인을 한다")
    void memberLogin() throws Exception {
        Member member = Member.builder()
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        AuthPrincipal authPrincipal = new AuthPrincipal(member);
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");

        given(userDetailsService.loadUserByUsername(anyString())).willReturn(authPrincipal);
        given(tokenService.tokenSave(any(Member.class))).willReturn(tokenResponse);

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "yoon1234")
                        .param("password", "12345678")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 시 아이디 또는 비밀번호가 일치하지 않으면 예외가 발생한다")
    void memberLogin_badCredentials() throws Exception {
        Member member = Member.builder()
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        AuthPrincipal authPrincipal = new AuthPrincipal(member);

        given(userDetailsService.loadUserByUsername(anyString())).willReturn(authPrincipal);

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "yoon1234")
                        .param("password", "87654321")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("E401001"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃을 한다")
    void memberLogout() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(tokenService).tokenDelete(anyString());

        mockMvc.perform(post("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 시 Refresh Token이 존재하지 않으면 예외가 발생한다")
    void memberLogout_invalidToken() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new InvalidTokenException()).given(tokenService).tokenDelete(anyString());

        mockMvc.perform(post("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("E401002"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("토큰이 유효하지 않습니다."))
                .andDo(print());
    }

    private String createAccessToken() {
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

    private Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}