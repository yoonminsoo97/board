package com.board.domain.post.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostListItem {

    private final Long postId;
    private final String title;
    private final String writer;
    private final int commentCount;
    private final LocalDateTime createdAt;

    public PostListItem(Long postId, String title, String writer, int commentCount, LocalDateTime createdAt) {
        this.postId = postId;
        this.title = title;
        this.writer = writer;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

}
