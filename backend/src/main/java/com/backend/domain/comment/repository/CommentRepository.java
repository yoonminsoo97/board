package com.backend.domain.comment.repository;

import com.backend.domain.comment.entity.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment AS c WHERE c.post.id = :postId AND c.id = :commentId")
    Optional<Comment> findByPostIdAndCommentId(@Param("postId") Long postId, @Param("commentId") Long commentId);

    @Query("DELETE FROM Comment AS c WHERE c.post.id = :postId")
    @Modifying
    void deleteByPostId(@Param("postId") Long postId);

    Page<Comment> findAllByPostId(Long postId, Pageable pageable);

}
