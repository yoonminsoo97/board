package com.board.domain.post.service;

import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
import com.board.domain.post.repository.PostRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .build();
    }

    @Test
    @DisplayName("게시글을 작성한다")
    void postWrite() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");
        Post post = Post.builder()
                .title(postWriteRequest.getTitle())
                .content(postWriteRequest.getContent())
                .member(member)
                .build();

        given(memberRepository.findMemberByUsername(anyString())).willReturn(Optional.of(member));
        given(postRepository.save(any(Post.class))).willReturn(post);

        postService.postWrite(postWriteRequest, "yoon1234");

        then(memberRepository).should().findMemberByUsername(anyString());
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void postWrite_notFoundPost() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");

        willThrow(new NotFoundMemberException()).given(memberRepository).findMemberByUsername(anyString());

        assertThatThrownBy(() -> postService.postWrite(postWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundMemberException.class);

        then(memberRepository).should().findMemberByUsername(anyString());
        then(postRepository).should(never()).save(any(Post.class));
    }

}