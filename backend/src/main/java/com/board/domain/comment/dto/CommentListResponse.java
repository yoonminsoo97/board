package com.board.domain.comment.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentListResponse {

    private List<CommentItem> comments;
    private int page;
    private long totalPages;
    private long totalComments;
    private boolean first;
    private boolean last;
    private boolean prev;
    private boolean next;

    @Builder
    private CommentListResponse(List<CommentItem> comments,
                               int page,
                               long totalComments,
                               long totalPages,
                               boolean first,
                               boolean last,
                               boolean prev,
                               boolean next) {
        this.comments = comments;
        this.page = page;
        this.totalComments = totalComments;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.prev = prev;
        this.next = next;
    }

    public static CommentListResponse of(Page<CommentItem> commentPage, long totalComments) {
        return CommentListResponse.builder()
                .comments(commentPage.getContent())
                .page(commentPage.getNumber() + 1)
                .totalPages(commentPage.getTotalPages())
                .totalComments(totalComments)
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .prev(commentPage.hasPrevious())
                .next(commentPage.hasNext())
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommentItem {

        private Long commentId;
        private String writer;
        private String content;
        private LocalDateTime createdAt;
        private boolean isDelete;
        private List<ReplyItem> replies;

        @Builder
        public CommentItem(Long commentId,
                           String writer,
                           String content,
                           LocalDateTime createdAt,
                           boolean isDelete) {
            this.commentId = commentId;
            this.writer = writer;
            this.content = content;
            this.createdAt = createdAt;
            this.isDelete = isDelete;
            this.replies = new ArrayList<>();
        }

        @Getter
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ReplyItem {

            private Long commentId;
            private Long referenceId;
            private String writer;
            private String content;
            private LocalDateTime createdAt;
            private boolean isDelete;

            @Builder
            public ReplyItem(Long commentId,
                             Long referenceId,
                             String writer,
                             String content,
                             LocalDateTime createdAt,
                             boolean isDelete) {
                this.commentId = commentId;
                this.referenceId = referenceId;
                this.writer = writer;
                this.content = content;
                this.createdAt = createdAt;
                this.isDelete = isDelete;
            }

        }

    }

}
