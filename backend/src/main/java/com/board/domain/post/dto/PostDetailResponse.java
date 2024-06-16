package com.board.domain.post.dto;

import com.board.domain.post.entity.Post;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDetailResponse {

    private Long postId;
    private String title;
    private String writer;
    private String content;
    private LocalDateTime createdAt;

    public static PostDetailResponse of(Post post) {
        return PostDetailResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .writer(post.getWriter())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .build();
    }

}
