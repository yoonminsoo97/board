package com.board.domain.post.repository;

import com.board.domain.post.dto.PostListResponse;

import org.springframework.data.domain.Pageable;

public interface PostCustomRepository {

    PostListResponse findPostList(Pageable pageable);
    PostListResponse findPostSearchList(Pageable pageable, String type, String keyword);
    PostListResponse findPostMemberList(Pageable pageable, Long memberId);

}
