package com.board.domain.member.controller;

import com.board.domain.comment.dto.CommentListItem;
import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.comment.service.CommentService;
import com.board.domain.member.dto.MemberProfileResponse;
import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.service.MemberService;
import com.board.domain.post.dto.PostListItem;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.service.PostService;
import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.exception.InvalidTokenException;
import com.board.global.security.dto.AuthPrincipal;

import com.board.support.RestDocsTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
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
class MemberControllerTest extends RestDocsTestSupport {

    @MockBean
    private MemberService memberService;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("닉네임 중복 확인을 한다")
    void memberNicknameExists() throws Exception {
        willDoNothing().given(memberService).memberNicknameExists(anyString());

        mockMvc.perform(get("/api/members/nickname/{nickname}", "yoonkun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
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
    @DisplayName("닉네임 중복 시 예외가 발생한다")
    void memberNicknameExistsDuplicateNickname() throws Exception {
        willThrow(new DuplicateNicknameException()).given(memberService).memberNicknameExists(anyString());

        mockMvc.perform(get("/api/members/nickname/{nickname}", "yoonkun"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.result.path").value("/api/members/nickname/yoonkun"))
                .andExpect(jsonPath("$.result.error.code").value("E409001"))
                .andExpect(jsonPath("$.result.error.message").value("사용 중인 닉네임입니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("nickname").description("닉네임")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("아이디 중복 확인을 한다")
    void memberUsernameExists() throws Exception {
        willDoNothing().given(memberService).memberUsernameExists(anyString());

        mockMvc.perform(get("/api/members/username/{username}", "yoon1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
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
    @DisplayName("아이디 중복 시 예외가 발생한다")
    void memberUsernameExistsDuplicateUsername() throws Exception {
        willThrow(new DuplicateUsernameException()).given(memberService).memberUsernameExists(anyString());

        mockMvc.perform(get("/api/members/username/{username}", "yoon1234"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.result.path").value("/api/members/username/yoon1234"))
                .andExpect(jsonPath("$.result.error.code").value("E409002"))
                .andExpect(jsonPath("$.result.error.message").value("사용 중인 아이디입니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("username").description("아이디")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
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
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
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
    @DisplayName("회원가입 시 아이디를 입력하지 않으면 예외가 발생한다")
    void memberSignupInvalidInputValue() throws Exception {
        MemberSignupRequest invalidMemberSignupRequest = new MemberSignupRequest("yoonkun", "", "12345678", "12345678");

        willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMemberSignupRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/members/signup"))
                .andExpect(jsonPath("$.result.error.code").value("E400001"))
                .andExpect(jsonPath("$.result.error.message").value("입력값이 잘못되었습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].field").value("username"))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].input").value(""))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].message").value("아이디를 입력해 주세요."))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("nickname").type(STRING).description("닉네임"),
                                fieldWithPath("username").type(STRING).description("아이디"),
                                fieldWithPath("password").type(STRING).description("비밀번호"),
                                fieldWithPath("passwordConfirm").type(STRING).description("비밀번호 확인")
                        ),
                        responseFields(
                                commonErrorResponse())
                                .and(
                                        fieldWithPath("result.error.fieldErrors[].field").description(STRING).description("필드명"),
                                        fieldWithPath("result.error.fieldErrors[].input").description(STRING).description("입력값"),
                                        fieldWithPath("result.error.fieldErrors[].message").description(STRING).description("메시지")
                                )
                ));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 일치하지 않으면 예외가 발생한다")
    void memberSignupPasswordMismatch() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678", "12345679");

        willThrow(new PasswordMismatchException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/members/signup"))
                .andExpect(jsonPath("$.result.error.code").value("E400002"))
                .andExpect(jsonPath("$.result.error.message").value("비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
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
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.accessToken").value("access-token"))
                .andExpect(jsonPath("$.result.refreshToken").value("refresh-token"))
                .andDo(restDocs.document(
                        formParameters(
                                parameterWithName("username").description("아이디"),
                                parameterWithName("password").description("비밀번호")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.accessToken").type(STRING).description("액세스 토큰"),
                                        fieldWithPath("result.refreshToken").type(STRING).description("리프레시 토큰")
                                )
                ));
    }

    @Test
    @DisplayName("로그인 시 아이디 또는 비밀번호가 일치하지 않으면 예외가 발생한다")
    void memberLoginBadCredentials() throws Exception {
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
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.result.path").value("/api/members/login"))
                .andExpect(jsonPath("$.result.error.code").value("E401001"))
                .andExpect(jsonPath("$.result.error.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
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

    @Test
    @DisplayName("로그아웃을 한다")
    void memberLogout() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(tokenService).tokenDelete(anyString());

        mockMvc.perform(post("/api/members/logout")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
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
    @DisplayName("로그아웃 시 Refresh Token이 존재하지 않으면 예외가 발생한다")
    void memberLogoutInvalidToken() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new InvalidTokenException()).given(tokenService).tokenDelete(anyString());

        mockMvc.perform(post("/api/members/logout")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.result.path").value("/api/members/logout"))
                .andExpect(jsonPath("$.result.error.code").value("E401002"))
                .andExpect(jsonPath("$.result.error.message").value("토큰이 유효하지 않습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
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
    @DisplayName("회원 정보를 조회한다")
    void memberProfile() throws Exception {
        MemberProfileResponse memberProfileResponse = new MemberProfileResponse("yoonkun", "yoon1234");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        given(memberService.memberProfile(anyString())).willReturn(memberProfileResponse);

        mockMvc.perform(get("/api/members/profile")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.nickname").value("yoonkun"))
                .andExpect(jsonPath("$.result.username").value("yoon1234"))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.nickname").type(STRING).description("회원 닉네임"),
                                        fieldWithPath("result.username").type(STRING).description("회원 아이디")
                                )
                ));
    }

    @Test
    @DisplayName("회원 정보 조회 시 회원을 찾을 수 없으면 예외가 발생한다")
    void memberProfileNotFoundMember() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundMemberException()).given(memberService).memberProfile(anyString());

        mockMvc.perform(get("/api/members/profile")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/members/profile"))
                .andExpect(jsonPath("$.result.error.code").value("E404001"))
                .andExpect(jsonPath("$.result.error.message").value("회원을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
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
    @DisplayName("회원 정보에서 작성한 게시글 목록을 조회한다")
    void memberProfilePostList() throws Exception {
        List<PostListItem> posts = List.of(
                new PostListItem(1L, "제목", "작성자", 5, LocalDateTime.of(2024, 6, 17, 0, 0))
        );
        PostListResponse postListResponse = new PostListResponse(posts, 1, 1, 1, false, false, true, true);

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        given(postService.postListFromMember(anyInt(), anyString())).willReturn(postListResponse);

        mockMvc.perform(get("/api/members/profile/posts")
                        .param("page", "1")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.posts[0].postNumber").value(1))
                .andExpect(jsonPath("$.result.posts[0].title").value("제목"))
                .andExpect(jsonPath("$.result.posts[0].writer").value("작성자"))
                .andExpect(jsonPath("$.result.posts[0].commentCount").value(5))
                .andExpect(jsonPath("$.result.posts[0].createdAt").value("2024.06.17"))
                .andExpect(jsonPath("$.result.pageNumber").value(1))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.prev").value(false))
                .andExpect(jsonPath("$.result.next").value(false))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(true))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.posts").type(ARRAY).description("게시글 목록"),
                                        fieldWithPath("result.posts[].postNumber").type(NUMBER).description("게시글 번호"),
                                        fieldWithPath("result.posts[].title").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.posts[].writer").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.posts[].commentCount").type(NUMBER).description("댓글 개수"),
                                        fieldWithPath("result.posts[].createdAt").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.pageNumber").type(NUMBER).description("페이지 번호"),
                                        fieldWithPath("result.totalPages").type(NUMBER).description("전체 페이지 개수"),
                                        fieldWithPath("result.totalElements").type(NUMBER).description("전체 게시글 개수"),
                                        fieldWithPath("result.prev").type(BOOLEAN).description("이전 페이지 이동 가능 여부"),
                                        fieldWithPath("result.next").type(BOOLEAN).description("다음 페이지 이동 가능 여부"),
                                        fieldWithPath("result.first").type(BOOLEAN).description("첫 번째 페이지 여부"),
                                        fieldWithPath("result.last").type(BOOLEAN).description("마지막 페이지 여부")
                                )
                ));
    }

    @Test
    @DisplayName("회원정보에서 작성한 댓글 목록을 조회한다")
    void memberProfileCommentList() throws Exception {
        List<CommentListItem> comments = List.of(
                new CommentListItem(1L, "작성자", "댓글",  LocalDateTime.of(2024, 6, 17, 0, 0))
        );
        CommentListResponse commentListResponse = new CommentListResponse(comments, 1, 1, 1, false, false, true, true);

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        given(commentService.commentListFromMember(anyInt(), anyString())).willReturn(commentListResponse);

        mockMvc.perform(get("/api/members/profile/comments")
                        .header("Authorization", "Bearer access-token")
                        .param("page", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.comments[0].commentNum").value(1))
                .andExpect(jsonPath("$.result.comments[0].writer").value("작성자"))
                .andExpect(jsonPath("$.result.comments[0].content").value("댓글"))
                .andExpect(jsonPath("$.result.comments[0].createdAt").value("2024.06.17"))
                .andExpect(jsonPath("$.result.pageNumber").value(1))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.prev").value(false))
                .andExpect(jsonPath("$.result.next").value(false))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(true))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.comments").type(ARRAY).description("댓글 목록"),
                                        fieldWithPath("result.comments[].commentNum").type(NUMBER).description("댓글 번호"),
                                        fieldWithPath("result.comments[].writer").type(STRING).description("댓글 작성자"),
                                        fieldWithPath("result.comments[].content").type(STRING).description("댓글 내용"),
                                        fieldWithPath("result.comments[].createdAt").type(STRING).description("댓글 작성일"),
                                        fieldWithPath("result.pageNumber").type(NUMBER).description("페이지 번호"),
                                        fieldWithPath("result.totalPages").type(NUMBER).description("전체 페이지 개수"),
                                        fieldWithPath("result.totalElements").type(NUMBER).description("전체 게시글 개수"),
                                        fieldWithPath("result.prev").type(BOOLEAN).description("이전 페이지 이동 가능 여부"),
                                        fieldWithPath("result.next").type(BOOLEAN).description("다음 페이지 이동 가능 여부"),
                                        fieldWithPath("result.first").type(BOOLEAN).description("첫 번째 페이지 여부"),
                                        fieldWithPath("result.last").type(BOOLEAN).description("마지막 페이지 여부")
                                )
                ));
    }

}