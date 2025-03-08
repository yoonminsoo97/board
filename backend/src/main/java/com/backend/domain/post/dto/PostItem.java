package com.backend.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@ToString
public class PostItem {

    private Long postId;
    private String title;
    private String writer;
    private LocalDateTime createdAt;
    private long commentCount;

    public PostItem(Long postId, String title, String writer, LocalDateTime createdAt, long commentCount) {
        this.postId = postId;
        this.title = title;
        this.writer = writer;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
    }

}
