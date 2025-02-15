package com.board.domain.comment.repository;

import com.board.domain.comment.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByPostIdAndId(Long postId, Long commentId);

}
