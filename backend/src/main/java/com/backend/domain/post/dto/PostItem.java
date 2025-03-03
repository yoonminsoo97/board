package com.backend.domain.post.dto;

import com.backend.domain.post.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostItem {

    private Long postId;
    private String title;
    private String writer;
    private LocalDateTime createdAt;

    public PostItem(Post post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.writer = post.getWriter();
        this.createdAt = post.getCreatedAt();
    }

}
