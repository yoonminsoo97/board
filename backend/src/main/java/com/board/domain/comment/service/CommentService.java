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
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
import com.board.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int PAGE_SIZE = 10;
    private static final String PROPERTIES = "id";

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void commentWrite(Long postId, CommentWriteRequest commentWriteRequest, Long memberId) {
        Post post = postRepository.findByPostId(postId);
        Member member = memberRepository.findByMemberId(memberId);
        Comment comment = Comment.builder()
                .writer(member.getNickname())
                .content(commentWriteRequest.getContent())
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);
    }

    @Transactional
    public void replyWrite(Long postId, Long commentId, CommentWriteRequest commentWriteRequest, Long memberId) {
        Comment comment = commentRepository.findCommentByPostIdAndCommentId(postId, commentId)
                .orElseThrow(NotFoundCommentException::new);
        Member member = memberRepository.findByMemberId(memberId);
        Comment reply = Comment.builder()
                .writer(member.getNickname())
                .content(commentWriteRequest.getContent())
                .member(member)
                .post(comment.getPost())
                .reference(comment)
                .build();
        comment.addReply(reply);
    }

    @Transactional(readOnly = true)
    public CommentListResponse commentList(Long postId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.Direction.ASC, PROPERTIES);
        Page<Comment> commentPage = commentRepository.findCommentsByPostId(pageable, postId);
        return new CommentListResponse(commentPage);
    }

    @Transactional
    public void commentModify(Long postId, Long commentId, CommentModifyRequest commentModifyRequest, Long memberId) {
        Comment comment = commentRepository.findCommentByPostIdAndCommentId(postId, commentId)
                .orElseThrow(NotFoundCommentException::new);
        if (!comment.isOwner(memberId)) {
            throw new CommentModifyAccessDeniedException();
        }
        if (comment.isDelete()) {
            throw new AlreadyDeleteCommentException();
        }
        comment.modify(commentModifyRequest.getContent());
    }

    @Transactional
    public void commentDelete(Long postId, Long commentId, Long memberId) {
        Comment comment = commentRepository.findCommentByPostIdAndCommentId(postId, commentId)
                .orElseThrow(NotFoundCommentException::new);
        if (!comment.isOwner(memberId)) {
            throw new CommentDeleteAccessDeniedException();
        }
        if (!comment.isReply()) {
            commentDelete(comment);
        } else {
            replyDelete(comment);
        }
    }

    private void commentDelete(Comment comment) {
        if (comment.isDelete()) {
            throw new AlreadyDeleteCommentException();
        }
        if (comment.hasReplies()) {
            comment.softDelete();
        } else {
            commentRepository.delete(comment);
        }
    }

    private void replyDelete(Comment reply) {
        Comment reference = reply.getReference();
        reference.deleteReply(reply);
        commentRepository.delete(reply);
        if (!reference.hasReplies() && reference.isDelete()) {
            commentRepository.delete(reference);
        }
    }

}
