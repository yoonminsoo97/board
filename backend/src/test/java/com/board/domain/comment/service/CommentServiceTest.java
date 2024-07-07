package com.board.domain.comment.service;

import com.board.domain.comment.dto.CommentListResponse;
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
import com.board.support.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class CommentServiceTest extends ServiceTest {

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
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(post, "id", 1L);
    }

    @Nested
    @DisplayName("댓글 작성")
    class CommentWriteTest {

        @Test
        @DisplayName("댓글을 작성한다")
        void commentWrite() {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("comment")
                    .build();

            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();

            given(postRepository.findByPostId(anyLong())).willReturn(post);
            given(memberRepository.findByMemberId(anyLong())).willReturn(member);
            given(commentRepository.save(any(Comment.class))).willReturn(comment);

            commentService.commentWrite(1L, commentWriteRequest, 1L);

            then(postRepository).should().findByPostId(anyLong());
            then(memberRepository).should().findByMemberId(anyLong());
            then(commentRepository).should().save(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글을 작성한다")
        void replyWrite() {
            CommentWriteRequest replyWriteRequest = CommentWriteRequest.builder()
                    .content("reply")
                    .build();

            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));
            given(memberRepository.findByMemberId(anyLong())).willReturn(member);

            commentService.replyWrite(1L, 1L, replyWriteRequest, 1L);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
            then(memberRepository).should().findByMemberId(anyLong());
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void commentWriteNotFoundPost() {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("comment")
                    .build();

            willThrow(new NotFoundPostException()).given(postRepository).findByPostId(anyLong());

            assertThatThrownBy(() -> commentService.commentWrite(1L, commentWriteRequest, 1L))
                    .isInstanceOf(NotFoundPostException.class);

            then(postRepository).should().findByPostId(anyLong());
            then(memberRepository).should(never()).findByMemberId(anyLong());
            then(commentRepository).should(never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
        void commentWriteNotFoundComment() {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("comment")
                    .build();

            given(postRepository.findByPostId(anyLong())).willReturn(post);
            willThrow(new NotFoundMemberException()).given(memberRepository).findByMemberId(anyLong());

            assertThatThrownBy(() -> commentService.commentWrite(1L, commentWriteRequest, 1L))
                    .isInstanceOf(NotFoundMemberException.class);

            then(postRepository).should().findByPostId(anyLong());
            then(memberRepository).should().findByMemberId(anyLong());
            then(commentRepository).should(never()).save(any(Comment.class));
        }

    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class CommentListFind {

        @Test
        @DisplayName("특정 게시글에 속한 댓글 목록을 조회한다")
        void commentList() {
            List<CommentListResponse.CommentItem> commentList = List.of(CommentListResponse.CommentItem.builder()
                    .commentId(1L)
                    .writer("yoonkun")
                    .content("comment")
                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                    .isDelete(false)
                    .build());
            List<CommentListResponse.CommentItem.ReplyItem> replyList = List.of(CommentListResponse.CommentItem.ReplyItem.builder()
                    .commentId(2L)
                    .referenceId(1L)
                    .writer("yoonkun")
                    .content("reply")
                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                    .isDelete(false)
                    .build());

            given(commentRepository.findCommentListByPostId(anyLong())).willReturn(commentList);
            given(commentRepository.findReplyListByPostId(anyLong())).willReturn(replyList);

            CommentListResponse commentListResponse = commentService.commentList(1L, 0);

            assertThat(commentListResponse.getPage()).isEqualTo(1);
            assertThat(commentListResponse.getTotalPages()).isEqualTo(1);
            assertThat(commentListResponse.getTotalComments()).isEqualTo(2);
            assertThat(commentListResponse.isFirst()).isTrue();
            assertThat(commentListResponse.isLast()).isTrue();
            assertThat(commentListResponse.isPrev()).isFalse();
            assertThat(commentListResponse.isNext()).isFalse();
            then(commentRepository).should().findCommentListByPostId(anyLong());
            then(commentRepository).should().findReplyListByPostId(anyLong());
        }

    }

    @Nested
    @DisplayName("댓글 수정")
    class CommentModifyTest {

        @Test
        @DisplayName("댓글을 수정한다")
        void commentModify() {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));

            commentService.commentModify(1L, 1L, commentModifyRequest, 1L);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("댓글이 존재하지 않으면 예외가 발생한다")
        void commentModifyNotFoundComment() {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            willThrow(new NotFoundCommentException()).given(commentRepository).findCommentByPostIdAndCommentId(anyLong(), anyLong());

            assertThatThrownBy(() -> commentService.commentModify(1L, 1L, commentModifyRequest, 1L))
                    .isInstanceOf(NotFoundCommentException.class);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("이미 삭제된 댓글이면 예외가 발생한다")
        void commentModifyAlreadyCommentDelete() {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            comment.softDelete();

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.commentModify(1L, 1L, commentModifyRequest, 1L))
                    .isInstanceOf(AlreadyDeleteCommentException.class);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("작성자가 아니면 예외가 발생한다")
        void commentModifyNotCommentOwner() {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.commentModify(1L, 1L, commentModifyRequest, 2L))
                    .isInstanceOf(CommentModifyAccessDeniedException.class);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        }

    }

    @Nested
    @DisplayName("댓글 삭제")
    class CommentDeleteTest {

        private Comment comment;

        @BeforeEach
        void setUp() {
            comment = Comment.builder()
                    .writer("yoonkun")
                    .content("comment")
                    .post(post)
                    .member(member)
                    .build();
        }

        @Test
        @DisplayName("대댓글이 존재하면 댓글을 논리 삭제한다")
        void commentSoftDeleteHasReplies() {
            Comment reply = Comment.builder()
                    .writer("yoonkun")
                    .content("reply")
                    .post(post)
                    .member(member)
                    .reference(comment)
                    .build();
            comment.addReply(reply);

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));

            commentService.commentDelete(1L, 1L, 1L);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
            then(commentRepository).should(never()).delete(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글이 존재하지 않으면 댓글을 물리 삭제한다")
        void commentHardDeleteHasNoReplies() {
            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));
            willDoNothing().given(commentRepository).delete(any(Comment.class));

            commentService.commentDelete(1L, 1L, 1L);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
            then(commentRepository).should().delete(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글을 삭제한다")
        void commentReplyDelete() {
            Comment reply = Comment.builder()
                    .writer("yoonkun")
                    .content("reply")
                    .post(post)
                    .member(member)
                    .reference(comment)
                    .build();
            comment.addReply(reply);

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(reply));
            willDoNothing().given(commentRepository).delete(any(Comment.class));

            commentService.commentDelete(1L, 1L, 1L);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
            then(commentRepository).should().delete(any(Comment.class));
        }

        @Test
        @DisplayName("논리 삭제된 댓글에 작성된 대댓글을 삭제 후 대댓글이 없는 경우 댓글도 물리 삭제한다")
        void commentDeleteReplyDeleteHasNoReplies() {
            Comment reply = Comment.builder()
                    .writer("yoonkun")
                    .content("reply")
                    .post(post)
                    .member(member)
                    .reference(comment)
                    .build();
            comment.addReply(reply);
            comment.softDelete();

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(reply));
            willDoNothing().given(commentRepository).delete(any(Comment.class));

            commentService.commentDelete(1L, 1L, 1L);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
            then(commentRepository).should(times(2)).delete(any(Comment.class));
        }

        @Test
        @DisplayName("댓글이 존재하지 않으면 예외가 발생한다")
        void commentDeleteNotFoundComment() {
            willThrow(new NotFoundCommentException()).given(commentRepository).findCommentByPostIdAndCommentId(anyLong(), anyLong());

            assertThatThrownBy(() -> commentService.commentDelete(1L, 1L, 1L))
                    .isInstanceOf(NotFoundCommentException.class);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("이미 삭제된 댓글이면 예외가 발생한다")
        void commentDeleteAlreadyCommentDelete() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            comment.softDelete();

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.commentDelete(1L, 1L, 1L))
                    .isInstanceOf(AlreadyDeleteCommentException.class);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("작성자가 아니면 예외가 발생한다")
        void commentDeleteNotCommentOwner() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();

            given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.commentDelete(1L, 1L, 2L))
                    .isInstanceOf(CommentDeleteAccessDeniedException.class);

            then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        }

    }

}