package com.backend.domain.comment.service;

import com.backend.domain.comment.dto.CommentListResponse;
import com.backend.domain.comment.dto.CommentModifyRequest;
import com.backend.domain.comment.dto.CommentWriteRequest;
import com.backend.domain.comment.entity.Comment;
import com.backend.domain.comment.exception.NotFoundCommentException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
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

    @DisplayName("댓글 목록을 조회한다.")
    @Test
    void commentList() {
        List<Comment> content = List.of(
                new Comment("writer", "comment", member, post)
        );
        PageImpl<Comment> commentPage = new PageImpl<>(content);

        given(commentRepository.findAllByPostId(anyLong(), any(Pageable.class))).willReturn(commentPage);

        CommentListResponse commentListResponse = commentService.commentList(1L, 1);

        assertThat(commentListResponse.getComments()).isNotEmpty();
        assertThat(commentListResponse.getPage()).isEqualTo(1);
        assertThat(commentListResponse.getTotalComments()).isEqualTo(1);
        assertThat(commentListResponse.getTotalPages()).isEqualTo(1);
        assertThat(commentListResponse.isFirst()).isTrue();
        assertThat(commentListResponse.isLast()).isTrue();
        assertThat(commentListResponse.isPrev()).isFalse();
        assertThat(commentListResponse.isNext()).isFalse();
        then(commentRepository).should().findAllByPostId(anyLong(), any(Pageable.class));
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

    @DisplayName("댓글을 수정한다.")
    @Test
    void commentModify() {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        Comment comment = Comment.builder()
                .writer(member.getNickname())
                .content("comment")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));

        commentService.commentModify(1L, 1L, commentModifyRequest);

        then(commentRepository).should().findByPostIdAndCommentId(anyLong(), anyLong());
    }

    @DisplayName("댓글 수정 시 댓글이 존재하지 않으면 예외가 발생한다.")
    @Test
    void commentModifyNotFoundComment() {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");

        willThrow(new NotFoundCommentException()).given(commentRepository).findByPostIdAndCommentId(anyLong(), anyLong());

        assertThatThrownBy(() -> commentService.commentModify(1L, 1L, commentModifyRequest))
                .isInstanceOf(NotFoundCommentException.class);

        then(commentRepository).should().findByPostIdAndCommentId(anyLong(), anyLong());
    }

    @DisplayName("댓글을 삭제한다.")
    @Test
    void commentDelete() {
        Comment comment = Comment.builder()
                .writer(member.getNickname())
                .content("comment")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));
        willDoNothing().given(commentRepository).delete(any(Comment.class));

        commentService.commentDelete(1L, 1L);

        then(commentRepository).should().findByPostIdAndCommentId(anyLong(), anyLong());
        then(commentRepository).should().delete(any(Comment.class));
    }

    @DisplayName("댓글 삭제 시 댓글이 존재하지 않으면 예외가 발생한다.")
    @Test
    void commentDeleteNotFoundComment() {
        willThrow(new NotFoundCommentException()).given(commentRepository).findByPostIdAndCommentId(anyLong(), anyLong());

        assertThatThrownBy(() -> commentService.commentDelete(1L, 1L))
                .isInstanceOf(NotFoundCommentException.class);

        then(commentRepository).should().findByPostIdAndCommentId(anyLong(), anyLong());
        then(commentRepository).should(never()).delete(any(Comment.class));
    }

}