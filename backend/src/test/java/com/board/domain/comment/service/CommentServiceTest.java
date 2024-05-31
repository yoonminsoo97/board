package com.board.domain.comment.service;

import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.entity.Comment;
import com.board.domain.comment.exception.AlreadyDeleteCommentException;
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

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
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
    @DisplayName("댓글 목록을 조회한다")
    void commentList() {
        List<Comment> content = List.of(
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build()
        );
        PageImpl<Comment> commentPage = new PageImpl<>(content);

        given(commentRepository.findCommentsByPostId(any(Pageable.class), anyLong())).willReturn(commentPage);

        commentService.commentList(1L, 1);

        then(commentRepository).should().findCommentsByPostId(any(Pageable.class), anyLong());
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
    @DisplayName("이미 삭제된 댓글에 수정을 시도할 경우 예외가 발생한다")
    void commentModifyAlreadyCommentDelete() {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();
        comment.delete();

        given(commentRepository.findCommentJoinFetchMember(anyLong(), anyLong())).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.commentModify(1L, 1L, commentModifyRequest, "yoon1234"))
                .isInstanceOf(AlreadyDeleteCommentException.class);

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

        commentService.commentDelete(1L, 1L, "yoon1234");

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 삭제 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void commentDeleteNotFoundComment() {
        willThrow(new NotFoundCommentException()).given(commentRepository).findCommentJoinFetchMember(anyLong(), anyLong());

        assertThatThrownBy(() -> commentService.commentDelete(1L, 1L, "yoon1234"))
                .isInstanceOf(NotFoundCommentException.class);

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
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
    }

    @Test
    @DisplayName("이미 삭제된 댓글에 삭제를 시도할 경우 예외가 발생한다")
    void commentDeleteAlreadyDeleteComment() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();
        comment.delete();

        given(commentRepository.findCommentJoinFetchMember(anyLong(), anyLong())).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.commentDelete(1L, 1L, "yoon1234"))
                .isInstanceOf(AlreadyDeleteCommentException.class);

        then(commentRepository).should().findCommentJoinFetchMember(anyLong(), anyLong());
    }

    @Test
    @DisplayName("특정 회원이 작성한 댓글 목록을 조회한다")
    void commentListFromMember() {
        List<Comment> content = List.of(
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build()
        );
        PageImpl<Comment> commentPage = new PageImpl<>(content);

        given(commentRepository.findCommentsByMemberUsername(any(Pageable.class), anyString())).willReturn(commentPage);

        commentService.commentListFromMember(0, "yoon1234");

        then(commentRepository).should().findCommentsByMemberUsername(any(Pageable.class), anyString());
    }

    @Test
    @DisplayName("대댓글을 작성한다")
    void replyWrite() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        CommentWriteRequest replyWriteRequest = new CommentWriteRequest("대댓글");

        given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));
        given(memberRepository.findMemberByUsername(anyString())).willReturn(Optional.of(member));

        commentService.replyWrite(1L, 1L, replyWriteRequest, "yoon1234");

        then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        then(memberRepository).should().findMemberByUsername(anyString());

    }

    @Test
    @DisplayName("대댓글 작성 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void replyWriteNotFoundComment() {
        CommentWriteRequest replyWriteRequest = new CommentWriteRequest("대댓글");

        willThrow(new NotFoundCommentException()).given(commentRepository).findCommentByPostIdAndCommentId(anyLong(), anyLong());

        assertThatThrownBy(() -> commentService.replyWrite(1L, 1L, replyWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundCommentException.class);

        then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        then(memberRepository).should(never()).findMemberByUsername(anyString());
    }

    @Test
    @DisplayName("대댓글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void replyWriteNotFoundMember() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        CommentWriteRequest replyWriteRequest = new CommentWriteRequest("대댓글");

        given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));
        willThrow(new NotFoundMemberException()).given(memberRepository).findMemberByUsername(anyString());

        assertThatThrownBy(() -> commentService.replyWrite(1L, 1L, replyWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundMemberException.class);

        then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        then(memberRepository).should().findMemberByUsername(anyString());
    }

}