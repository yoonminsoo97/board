package com.board.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostListItem {

    private final Long postNumber;
    private final String title;
    private final String writer;
    private final int commentCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private final LocalDateTime createdAt;

    public PostListItem(Long postNumber, String title, String writer, int commentCount, LocalDateTime createdAt) {
        this.postNumber = postNumber;
        this.title = title;
        this.writer = writer;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

}
