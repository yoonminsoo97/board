package com.backend.domain.comment.service;

import com.backend.domain.comment.dto.CommentWriteRequest;
import com.backend.domain.comment.entity.Comment;
import com.backend.domain.comment.repository.CommentRepository;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.exception.NotFoundMemberException;
import com.backend.domain.member.repository.MemberRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
    }

    @DisplayName("댓글을 작성한다.")
    @Test
    void commentWrite() {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");
        Comment comment = Comment.builder()
                .writer(member.getNickname())
                .content(commentWriteRequest.getContent())
                .member(member)
                .post(post)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        commentService.commentWrite(1L, "yoon1234", commentWriteRequest);

        then(postRepository).should().findById(anyLong());
        then(memberRepository).should().findByUsername(anyString());
        then(commentRepository).should().save(any(Comment.class));
    }

    @DisplayName("댓글 작성 시 게시글이 존재하지 않으면 예외가 발생한다.")
    @Test
    void commentWriteNotFoundPost() {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");

        willThrow(new NotFoundPostException()).given(postRepository).findById(anyLong());

        assertThatThrownBy(() -> commentService.commentWrite(1L, "yoon1234", commentWriteRequest))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findById(anyLong());
        then(memberRepository).should(never()).findByUsername(anyString());
        then(commentRepository).should(never()).save(any(Comment.class));
    }

    @DisplayName("댓글 작성 시 회원이 존재하지 않으면 예외가 발생한다.")
    @Test
    void commentWriteNotFoundMember() {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        willThrow(new NotFoundMemberException()).given(memberRepository).findByUsername(anyString());

        assertThatThrownBy(() -> commentService.commentWrite(1L, "yoon1234", commentWriteRequest))
                .isInstanceOf(NotFoundMemberException.class);

        then(postRepository).should().findById(anyLong());
        then(memberRepository).should().findByUsername(anyString());
        then(commentRepository).should(never()).save(any(Comment.class));
    }

}