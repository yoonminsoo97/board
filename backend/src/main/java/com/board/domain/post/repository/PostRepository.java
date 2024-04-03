package com.board.domain.post.repository;

import com.board.domain.post.entity.Post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository {

    @Query("SELECT p FROM Post AS p JOIN FETCH p.member WHERE p.id = :postId")
    Optional<Post> findPostJoinFetch(@Param("postId") Long postId);

}
