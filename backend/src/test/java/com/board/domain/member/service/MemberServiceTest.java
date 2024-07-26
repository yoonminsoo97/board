package com.board.domain.member.service;

import com.board.domain.comment.repository.CommentRepository;
import com.board.domain.member.dto.MemberCommentListResponse;
import com.board.domain.member.dto.MemberCommentListResponse.CommentItem;
import com.board.domain.member.dto.MemberNicknameRequest;
import com.board.domain.member.dto.MemberPasswordRequest;
import com.board.domain.member.dto.MemberProfileResponse;
import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostListResponse.PostItem;
import com.board.domain.post.repository.PostRepository;
import com.board.support.ServiceTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

class MemberServiceTest extends ServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("닉네임 중복 확인")
    class MemberNicknameExistsTest {

        @Test
        @DisplayName("닉네임이 중복되지 않는다")
        void memberNicknameExists() {
            given(memberRepository.existsByNickname(anyString())).willReturn(false);

            memberService.memberNicknameExists("yoonkun");

            then(memberRepository).should().existsByNickname(anyString());
        }

        @Test
        @DisplayName("닉네임이 중복되면 예외가 발생한다")
        void memberNicknameExistsDuplicateNickname() {
            given(memberRepository.existsByNickname(anyString())).willReturn(true);

            assertThatThrownBy(() -> memberService.memberNicknameExists("yoonkun"))
                    .isInstanceOf(DuplicateNicknameException.class);

            then(memberRepository).should().existsByNickname(anyString());
        }

    }

    @Nested
    @DisplayName("아이디 중복 확인")
    class MemberUsernameExistsTest {

        @Test
        @DisplayName("아이디가 중복되지 않는다")
        void memberUsernameExists() {
            given(memberRepository.existsByUsername(anyString())).willReturn(false);

            memberService.memberUsernameExists("yoonkun");

            then(memberRepository).should().existsByUsername(anyString());
        }

        @Test
        @DisplayName("아이디가 중복되면 예외가 발생한다")
        void memberUsernameExistsDuplicateUsername() {
            given(memberRepository.existsByUsername(anyString())).willReturn(true);

            assertThatThrownBy(() -> memberService.memberUsernameExists("yoonkun"))
                    .isInstanceOf(DuplicateUsernameException.class);

            then(memberRepository).should().existsByUsername(anyString());
        }

    }

    @Nested
    @DisplayName("회원가입")
    class MemberSignupTest {

        @Test
        @DisplayName("회원가입을 한다")
        void memberSignup() {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            String encoded = new BCryptPasswordEncoder().encode("12345678");

            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(encoded)
                    .build();

            given(memberRepository.existsByNickname(anyString())).willReturn(false);
            given(memberRepository.existsByUsername(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn(encoded);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            memberService.memberSignup(memberSignupRequest);

            then(memberRepository).should().existsByNickname(anyString());
            then(memberRepository).should().existsByUsername(anyString());
            then(passwordEncoder).should().encode(anyString());
            then(memberRepository).should().save(any(Member.class));

        }

        @Test
        @DisplayName("닉네임이 중복되면 예외가 발생한다")
        void memberSignupDuplicateNickname() {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            given(memberRepository.existsByNickname(anyString())).willReturn(true);

            assertThatThrownBy(() -> memberService.memberSignup(memberSignupRequest))
                    .isInstanceOf(DuplicateNicknameException.class);

            then(memberRepository).should().existsByNickname(anyString());
            then(memberRepository).should(never()).existsByUsername(anyString());
            then(passwordEncoder).should(never()).encode(anyString());
            then(memberRepository).should(never()).save(any(Member.class));
        }

        @Test
        @DisplayName("아이디가 중복되면 예외가 발생한다")
        void memberSignupDuplicateUsername() {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("12345678")
                    .build();

            given(memberRepository.existsByNickname(anyString())).willReturn(false);
            given(memberRepository.existsByUsername(anyString())).willReturn(true);

            assertThatThrownBy(() -> memberService.memberSignup(memberSignupRequest))
                    .isInstanceOf(DuplicateUsernameException.class);

            then(memberRepository).should().existsByNickname(anyString());
            then(memberRepository).should().existsByUsername(anyString());
            then(passwordEncoder).should(never()).encode(anyString());
            then(memberRepository).should(never()).save(any(Member.class));
        }

        @Test
        @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 예외가 발생한다")
        void memberSignupPassworndAndPasswordConfirmMismatch() {
            MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password("12345678")
                    .passwordConfirm("87654321")
                    .build();

            given(memberRepository.existsByNickname(anyString())).willReturn(false);
            given(memberRepository.existsByUsername(anyString())).willReturn(false);

            assertThatThrownBy(() -> memberService.memberSignup(memberSignupRequest))
                    .isInstanceOf(PasswordMismatchException.class);

            then(memberRepository).should().existsByNickname(anyString());
            then(memberRepository).should().existsByUsername(anyString());
            then(passwordEncoder).should(never()).encode(anyString());
            then(memberRepository).should(never()).save(any(Member.class));
        }

    }

    @Nested
    @DisplayName("회원 상세정보")
    class MemberProfileTest {

        @Test
        @DisplayName("상제정보를 조회한다")
        void memberProfile() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            given(memberRepository.findByMemberId(anyLong())).willReturn(member);

            MemberProfileResponse memberProfileResponse = memberService.memberProfile(1L);

            assertThat(memberProfileResponse.getNickname()).isEqualTo("yoonkun");
            assertThat(memberProfileResponse.getUsername()).isEqualTo("yoon1234");
            then(memberRepository).should().findByMemberId(anyLong());
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 에외가 발생한다")
        void memberProfileNotFoundMember() {
            willThrow(new NotFoundMemberException()).given(memberRepository).findByMemberId(anyLong());

            assertThatThrownBy(() -> memberService.memberProfile(1L))
                    .isInstanceOf(NotFoundMemberException.class);

            then(memberRepository).should().findByMemberId(anyLong());
        }

    }

    @Nested
    @DisplayName("회원이 작성한 게시글 목록 조회")
    class MemberPostListTest {

        @Test
        @DisplayName("회원이 작성한 게시글 목록을 조회한다")
        void memberPostList() {
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(List.of(
                            PostItem.builder()
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

            given(postRepository.findPostMemberList(any(Pageable.class), anyLong())).willReturn(postListResponse);

            PostListResponse response = memberService.memberPostList(0, 1L);

            assertThat(response.getPosts().get(0).getPostId()).isEqualTo(1L);
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isPrev()).isFalse();
            assertThat(response.isNext()).isFalse();
            then(postRepository).should().findPostMemberList(any(Pageable.class), anyLong());
        }

    }

    @Nested
    @DisplayName("회원이 작성한 댓글 목록 조회")
    class MemberCommentListTest {

        @Test
        @DisplayName("회원이 작성한 댓글 목록을 조회한다")
        void memberCommentList() {
            MemberCommentListResponse memberCommentListResponse = MemberCommentListResponse.builder()
                    .comments(List.of(
                            CommentItem.builder()
                                    .commentId(1L)
                                    .writer("yoonkun")
                                    .content("comment")
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

            given(commentRepository.findMemberCommentList(any(Pageable.class), anyLong())).willReturn(memberCommentListResponse);

            MemberCommentListResponse response = memberService.memberCommentList(0, 1L);

            assertThat(response.getComments().get(0).getCommentId()).isEqualTo(1L);
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isPrev()).isFalse();
            assertThat(response.isNext()).isFalse();
            then(commentRepository).should().findMemberCommentList(any(Pageable.class), anyLong());
        }

    }

    @Nested
    @DisplayName("회원 닉네임 변경")
    class MemberNicknameChangeTest {

        @Test
        @DisplayName("닉네임을 변경한다")
        void memberNicknameChange() {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("newNickname")
                    .build();

            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            given(memberRepository.findByMemberId(anyLong())).willReturn(member);

            memberService.memberNicknameChange(memberNicknameRequest, 1L);

            then(memberRepository).should().findByMemberId(anyLong());
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
        void memberNicknameChangeNotFoundMember() {
            MemberNicknameRequest memberNicknameRequest = MemberNicknameRequest.builder()
                    .nickname("newNickname")
                    .build();

            willThrow(new NotFoundMemberException()).given(memberRepository).findByMemberId(anyLong());

            assertThatThrownBy(() -> memberService.memberNicknameChange(memberNicknameRequest, 1L))
                    .isInstanceOf(NotFoundMemberException.class);

            then(memberRepository).should().findByMemberId(anyLong());
        }

    }

    @Nested
    @DisplayName("회원 비밀번호 변경")
    class MemberPasswordChangeTest {

        @Test
        @DisplayName("비밀번호를 변경한다")
        void memberPasswordChange() {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            given(memberRepository.findByMemberId(anyLong())).willReturn(member);
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            memberService.memberPasswordChange(memberPasswordRequest, 1L);

            then(memberRepository).should().findByMemberId(anyLong());
            then(passwordEncoder).should().matches(anyString(), anyString());
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
        void memberPasswordChangeNotFoundMember() {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("87654321")
                    .build();

            willThrow(new NotFoundMemberException()).given(memberRepository).findByMemberId(anyLong());

            assertThatThrownBy(() -> memberService.memberPasswordChange(memberPasswordRequest, 1L))
                    .isInstanceOf(NotFoundMemberException.class);

            then(memberRepository).should().findByMemberId(anyLong());
            then(passwordEncoder).should(never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("현재 사용 중인 비밀번호가 일치하지 않으면 예외가 발생한다")
        void MemberPasswordCurPasswordMismatch() {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345679")
                    .newPassword("87654321")
                    .newPasswordConfirm("98765432")
                    .build();

            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            given(memberRepository.findByMemberId(anyLong())).willReturn(member);
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            assertThatThrownBy(() -> memberService.memberPasswordChange(memberPasswordRequest, 1L))
                    .isInstanceOf(PasswordMismatchException.class);

            then(memberRepository).should().findByMemberId(anyLong());
            then(passwordEncoder).should().matches(anyString(), anyString());
        }

        @Test
        @DisplayName("새 비밀번호와 새 비밀번호 확인이 일치하지 않으면 예외가 발생한다")
        void memberPasswordNewPasswordAndNewPasswordConfirmMismatch() {
            MemberPasswordRequest memberPasswordRequest = MemberPasswordRequest.builder()
                    .curPassword("12345678")
                    .newPassword("87654321")
                    .newPasswordConfirm("98765432")
                    .build();

            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            given(memberRepository.findByMemberId(anyLong())).willReturn(member);
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            assertThatThrownBy(() -> memberService.memberPasswordChange(memberPasswordRequest, 1L))
                    .isInstanceOf(PasswordMismatchException.class);

            then(memberRepository).should().findByMemberId(anyLong());
            then(passwordEncoder).should().matches(anyString(), anyString());
        }


    }

}