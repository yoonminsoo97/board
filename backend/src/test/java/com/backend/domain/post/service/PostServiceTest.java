package com.backend.domain.post.service;

import com.backend.domain.member.entity.Member;
import com.backend.domain.member.exception.NotFoundMemberException;
import com.backend.domain.member.repository.MemberRepository;
import com.backend.domain.post.dto.PostDetailResponse;
import com.backend.domain.post.dto.PostWriteRequest;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.exception.NotFoundPostException;
import com.backend.domain.post.repository.PostRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
                .password("12345678")
                .build();
    }

    @DisplayName("게시글을 작성한다.")
    @Test
    void postWrite() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(postRepository.save(any(Post.class))).willReturn(post);

        postService.postWrite(postWriteRequest, "yoon1234");

        then(memberRepository).should().findByUsername(anyString());
        then(postRepository).should().save(any(Post.class));
    }

    @DisplayName("게시글 작성 시 회원이 존재하지 않으면 예외가 발생한다.")
    @Test
    void postWriteNotFoundMember() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");

        willThrow(new NotFoundMemberException()).given(memberRepository).findByUsername(anyString());

        assertThatThrownBy(() -> postService.postWrite(postWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundMemberException.class);

        then(memberRepository).should().findByUsername(anyString());
        then(postRepository).should(never()).save(any(Post.class));
    }

    @DisplayName("게시글을 상세조회 한다.")
    @Test
    void postDetail() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        PostDetailResponse postDetailResponse = postService.postDetail(1L);

        assertThat(postDetailResponse.getTitle()).isEqualTo("title");
        assertThat(postDetailResponse.getWriter()).isEqualTo("yoonkun");
        assertThat(postDetailResponse.getContent()).isEqualTo("content");
        then(postRepository).should().findById(anyLong());
    }

    @DisplayName("게시글 상세조회 시 게시글이 존재하지 않으면 예외가 발생한다.")
    @Test
    void postDetailNotFoundPost() {
        willThrow(new NotFoundPostException()).given(postRepository).findById(anyLong());

        assertThatThrownBy(() -> postService.postDetail(1L))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findById(anyLong());
    }

}