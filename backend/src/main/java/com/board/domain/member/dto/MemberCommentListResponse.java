package com.board.domain.member.dto;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberCommentListResponse {

    private List<CommentItem> comments;
    private int page;
    private int totalPages;
    private long totalElements;
    private boolean prev;
    private boolean next;
    private boolean first;
    private boolean last;

    public static MemberCommentListResponse of(Page<CommentItem> commentPage) {
        return MemberCommentListResponse.builder()
                .comments(commentPage.getContent())
                .page(commentPage.getNumber() + 1)
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .prev(commentPage.hasPrevious())
                .next(commentPage.hasNext())
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    public static class CommentItem {

        private Long commentId;
        private String writer;
        private String content;
        private LocalDateTime createdAt;

    }

}
