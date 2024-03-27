package com.board.domain.comment.service;

import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.entity.Comment;
import com.board.domain.comment.exception.CommentDeleteAccessDeniedException;
import com.board.domain.comment.exception.CommentModifyAccessDeniedException;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.comment.repository.CommentRepository;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
import com.board.domain.post.exception.NotFoundPostException;
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
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CommentService commentService;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .build();

        post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
    }

    @Test
    @DisplayName("댓글을 작성한다")
    void commentWrite() {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("댓글");
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(memberRepository.findMemberByUsername(anyString())).willReturn(Optional.of(member));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        commentService.commentWrite(1L, commentWriteRequest, "yoon1234");

        then(postRepository).should().findById(anyLong());
        then(memberRepository).should().findMemberByUsername(anyString());
        then(commentRepository).should().save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void commentWriteNotFoundPost() {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("댓글");

        willThrow(new NotFoundPostException()).given(postRepository).findById(anyLong());

        assertThatThrownBy(() -> commentService.commentWrite(1L, commentWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findById(anyLong());
        then(memberRepository).should(never()).findMemberByUsername(anyString());
        then(commentRepository).should(never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void commentWriteNotFoundMember() {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("댓글");

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        willThrow(new NotFoundMemberException()).given(memberRepository).findMemberByUsername(anyString());

        assertThatThrownBy(() -> commentService.commentWrite(1L, commentWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundMemberException.class);

        then(postRepository).should().findById(anyLong());
        then(memberRepository).should().findMemberByUsername(anyString());
        then(commentRepository).should(never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void commentModify() {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findCommentJoinFetchMember(anyLong(), anyLong())).willReturn(Optional.of(comment));

        commentService.commentModify(1L, 1L, commentModifyRequest, "yoon1234");

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 수정 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void commentModifyNotFoundComment() {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");

        willThrow(new NotFoundCommentException()).given(commentRepository).findCommentJoinFetchMember(anyLong(), anyLong());

        assertThatThrownBy(() -> commentService.commentModify(1L, 1L, commentModifyRequest, "yoon1234"))
                .isInstanceOf(NotFoundCommentException.class);

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 수정 시 작성자가 아닌데 수정을 시도하면 예외가 발생한다")
    void commentModifyNotCommentOwner() {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findCommentJoinFetchMember(anyLong(), anyLong())).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.commentModify(1L, 1L, commentModifyRequest, "unknown"))
                .isInstanceOf(CommentModifyAccessDeniedException.class);

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void commentDelete() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findCommentJoinFetchMember(anyLong(), anyLong())).willReturn(Optional.of(comment));
        willDoNothing().given(commentRepository).delete(any(Comment.class));

        commentService.commentDelete(1L, 1L, "yoon1234");

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
        then(commentRepository).should().delete(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void commentDeleteNotFoundComment() {
        willThrow(new NotFoundCommentException()).given(commentRepository).findCommentJoinFetchMember(anyLong(), anyLong());

        assertThatThrownBy(() -> commentService.commentDelete(1L, 1L, "yoon1234"))
                .isInstanceOf(NotFoundCommentException.class);

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
        then(commentRepository).should(never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 시 작성자가 아닌데 삭제를 시도하면 예외가 발생한다")
    void commentDeleteNotCommentOwner() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findCommentJoinFetchMember(anyLong(), anyLong())).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.commentDelete(1L, 1L, "unknown"))
                .isInstanceOf(CommentDeleteAccessDeniedException.class);

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
        then(commentRepository).should(never()).delete(any(Comment.class));
    }

}