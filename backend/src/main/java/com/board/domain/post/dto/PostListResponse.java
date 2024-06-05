package com.board.domain.post.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PostListResponse {

    private List<PostItem> posts;
    private int page;
    private int totalPages;
    private long totalElements;
    private boolean prev;
    private boolean next;
    private boolean first;
    private boolean last;

    public static PostListResponse of(Page<PostItem> postPage) {
        return PostListResponse.builder()
                .posts(postPage.getContent())
                .page(postPage.getNumber() + 1)
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .prev(postPage.hasPrevious())
                .next(postPage.hasNext())
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    public static class PostItem {

        private Long postId;
        private String title;
        private String writer;
        private int commentCount;
        private LocalDateTime createdAt;

    }

}
