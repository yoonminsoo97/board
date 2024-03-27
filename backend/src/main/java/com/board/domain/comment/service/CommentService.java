package com.board.domain.comment.service;

import com.board.domain.comment.dto.CommentListResponse;
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

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void commentWrite(Long postNumber, CommentWriteRequest commentWriteRequest, String loginUsername) {
        Post post = postRepository.findById(postNumber)
                .orElseThrow(NotFoundPostException::new);
        Member member = memberRepository.findMemberByUsername(loginUsername)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = Comment.builder()
                .content(commentWriteRequest.getContent())
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public CommentListResponse commentList(Long postNumber, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.Direction.ASC, PROPERTIES);
        Page<Comment> commentPage = commentRepository.findCommentsByPostId(pageable, postNumber);
        return new CommentListResponse(commentPage);
    }

    @Transactional
    public void commentModify(Long postNumber, Long commentNumber, CommentModifyRequest commentModifyRequest, String loginUsername) {
        Comment comment = commentRepository.findCommentJoinFetchMember(postNumber, commentNumber)
                .orElseThrow(NotFoundCommentException::new);
        if (!comment.isOwner(loginUsername)) {
            throw new CommentModifyAccessDeniedException();
        }
        comment.modify(commentModifyRequest.getContent());
    }

    @Transactional
    public void commentDelete(Long postNumber, Long commentNumber, String loginUsername) {
        Comment comment = commentRepository.findCommentJoinFetchMember(postNumber, commentNumber)
                .orElseThrow(NotFoundCommentException::new);
        if (!comment.isOwner(loginUsername)) {
            throw new CommentDeleteAccessDeniedException();
        }
        commentRepository.delete(comment);
    }

}
