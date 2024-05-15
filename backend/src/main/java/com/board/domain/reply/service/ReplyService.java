package com.board.domain.reply.service;

import com.board.domain.comment.entity.Comment;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.comment.repository.CommentRepository;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.reply.dto.ReplyWriteRequest;
import com.board.domain.reply.entity.Reply;
import com.board.domain.reply.repository.ReplyRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    @Transactional
    public void replyWrite(String username, Long postId, Long commentId, ReplyWriteRequest replyWriteRequest) {
        Member member = memberRepository.findMemberByUsername(username)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = commentRepository.findCommentByPostIdAndCommentId(postId, commentId)
                .orElseThrow(NotFoundCommentException::new);
        Reply reply = Reply.builder()
                .content(replyWriteRequest.getContent())
                .member(member)
                .comment(comment)
                .build();
        replyRepository.save(reply);
    }

}
