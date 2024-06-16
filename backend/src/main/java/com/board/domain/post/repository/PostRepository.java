package com.board.domain.post.repository;

import com.board.domain.post.entity.Post;
import com.board.domain.post.exception.NotFoundPostException;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository {

    default Post findByPostId(Long postId) {
        return findById(postId)
                .orElseThrow(NotFoundPostException::new);
    }

}
