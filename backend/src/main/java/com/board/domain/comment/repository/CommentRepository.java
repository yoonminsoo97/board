package com.board.domain.comment.repository;

import com.board.domain.comment.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
