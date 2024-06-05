package com.board.domain.post.repository;

import com.board.domain.post.dto.PostListResponse;

import org.springframework.data.domain.Pageable;

public interface PostCustomRepository {

    PostListResponse findPosts(Pageable pageable);
    PostListResponse findSearchPosts(Pageable pageable, String type, String keyword);

}
