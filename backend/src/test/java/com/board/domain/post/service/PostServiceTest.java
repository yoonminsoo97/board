package com.board.domain.post.service;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
import com.board.domain.post.repository.PostRepository;

import com.board.domain.token.exception.NotFoundTokenException;
import org.assertj.core.api.Assertions;
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

    @Test
    @DisplayName("게시글을 작성한다.")
    void postWrite() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(postRepository.save(any(Post.class))).willReturn(post);

        postService.postWrite("yoon1234", postWriteRequest);

        then(memberRepository).should().findByUsername(anyString());
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 시 회원이 존재하지 않으면 예외가 발생한다.")
    void postWriteNotFoundMember() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");

        willThrow(new NotFoundTokenException()).given(memberRepository).findByUsername(anyString());

        assertThatThrownBy(() -> postService.postWrite("yoon1234", postWriteRequest))
                        .isInstanceOf(NotFoundTokenException.class);

        then(memberRepository).should().findByUsername(anyString());
        then(postRepository).should(never()).save(any(Post.class));
    }

}