package com.backend.domain.post.repository;

import com.backend.domain.post.dto.PostItem;
import com.backend.domain.post.entity.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT new com.backend.domain.post.dto.PostItem(p.id, p.title, p.writer, p.createdAt, COUNT(c.id)) FROM Post AS p " +
            "LEFT JOIN Comment AS c ON p.id = c.post.id GROUP BY p.id ORDER BY p.id DESC", countQuery = "SELECT COUNT(p) FROM Post AS p")
    Page<PostItem> findAllPosts(Pageable pageable);

}
