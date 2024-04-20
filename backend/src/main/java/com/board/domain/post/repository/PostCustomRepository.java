package com.board.domain.post.repository;

import com.board.domain.post.dto.PostListItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCustomRepository {

    Page<PostListItem> findPosts(Pageable pageable);
    Page<PostListItem> findPostsSearch(Pageable pageable, String type, String keyword);
    Page<PostListItem> findPostsFromMember(Pageable pageable, String username);

}
