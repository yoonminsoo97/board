package com.board.domain.reply.service;

import com.board.domain.comment.entity.Comment;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.comment.repository.CommentRepository;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
import com.board.domain.reply.dto.ReplyWriteRequest;
import com.board.domain.reply.entity.Reply;
import com.board.domain.reply.repository.ReplyRepository;

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
class ReplyServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReplyRepository replyRepository;

    @InjectMocks
    private ReplyService replyService;

    private Member member;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .build();
        post = Post.builder()
                .title("title")
                .content("content")
                .member(member)
                .build();
        comment = Comment.builder()
                .content("content")
                .member(member)
                .post(post)
                .build();
    }

    @Test
    @DisplayName("대댓글을 작성한다")
    void replyWrite() {
        ReplyWriteRequest replyWriteRequest = new ReplyWriteRequest("reply");

        Reply reply = Reply.builder()
                .content("reply")
                .member(member)
                .comment(comment)
                .build();

        given(memberRepository.findMemberByUsername(anyString())).willReturn(Optional.of(member));
        given(commentRepository.findCommentByPostIdAndCommentId(anyLong(), anyLong())).willReturn(Optional.of(comment));
        given(replyRepository.save(any(Reply.class))).willReturn(reply);

        replyService.replyWrite("yoon2134", 1L, 1L, replyWriteRequest);

        then(memberRepository).should().findMemberByUsername(anyString());
        then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        then(replyRepository).should().save(any(Reply.class));
    }

    @Test
    @DisplayName("대댓글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void replyWriteNotFoundMember() {
        ReplyWriteRequest replyWriteRequest = new ReplyWriteRequest("reply");

        willThrow(new NotFoundMemberException()).given(memberRepository).findMemberByUsername(anyString());

        assertThatThrownBy(() -> replyService.replyWrite("yoon1234", 1L, 1L, replyWriteRequest))
                .isInstanceOf(NotFoundMemberException.class);

        then(memberRepository).should().findMemberByUsername(anyString());
        then(commentRepository).should(never()).findCommentByPostIdAndCommentId(anyLong(), anyLong());
        then(replyRepository).should(never()).save(any(Reply.class));
    }

    @Test
    @DisplayName("대댓글 작성 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void replyWriteNotFoundComment() {
        ReplyWriteRequest replyWriteRequest = new ReplyWriteRequest("reply");

        given(memberRepository.findMemberByUsername(anyString())).willReturn(Optional.of(member));
        willThrow(new NotFoundCommentException()).given(commentRepository).findCommentByPostIdAndCommentId(anyLong(), anyLong());

        assertThatThrownBy(() -> replyService.replyWrite("yoon1234", 1L, 1L, replyWriteRequest))
                .isInstanceOf(NotFoundCommentException.class);

        then(memberRepository).should().findMemberByUsername(anyString());
        then(commentRepository).should().findCommentByPostIdAndCommentId(anyLong(), anyLong());
        then(replyRepository).should(never()).save(any(Reply.class));
    }

}