package com.board.domain.comment.repository;

import com.board.domain.comment.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment AS c JOIN FETCH c.member WHERE c.post.id = :postId AND c.id = :commentId")
    Optional<Comment> findCommentJoinFetchMember(@Param("postId") Long postId, @Param("commentId") Long commentId);

}
