package com.board.domain.post.dto;

import com.board.domain.post.entity.Post;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostListItem {

    private final Long postNumber;
    private final String title;
    private final String writer;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private final LocalDateTime createdAt;

    public PostListItem(Post post) {
        this.postNumber = post.getId();
        this.title = post.getTitle();
        this.writer = post.getWriter();
        this.createdAt = post.getCreatedAt();
    }

}
