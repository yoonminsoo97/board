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

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public CommentListResponse commentList(Long postId, int page) {
        page = page <= 0 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.ASC, "id");
        Page<Comment> commentPage = commentRepository.findAllByPostId(postId, pageable);
        return new CommentListResponse(commentPage);
    }

    @Transactional
    public void commentWrite(Long postId, String username, CommentWriteRequest commentWriteRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundPostException::new);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = Comment.builder()
                .writer(member.getNickname())
                .content(commentWriteRequest.getContent())
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);
    }

    @Transactional
    public void commentModify(Long postId, Long commentId, CommentModifyRequest commentModifyRequest) {
        Comment comment = commentRepository.findByPostIdAndCommentId(postId, commentId)
                .orElseThrow(NotFoundCommentException::new);
        comment.modify(commentModifyRequest.getContent());
    }

    @Transactional
    public void commentDelete(Long postId, Long commentId) {
        Comment comment = commentRepository.findByPostIdAndCommentId(postId, commentId)
                .orElseThrow(NotFoundCommentException::new);
        commentRepository.delete(comment);
    }

}
