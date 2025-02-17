package com.board.domain.comment.dto;

import com.board.domain.comment.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponse {

    private List<CommentItem> comments;
    private int page;
    private int totalPages;
    private long totalComments;
    private boolean first;
    private boolean last;
    private boolean prev;
    private boolean next;

    public CommentListResponse(Page<Comment> commentPage) {
        this.comments = commentPage.getContent()
                .stream()
                .map(CommentItem::new)
                .collect(Collectors.toList());
        this.page = commentPage.getNumber() + 1;
        this.totalPages = commentPage.getTotalPages();
        this.totalComments = commentPage.getTotalElements();
        this.first = commentPage.isFirst();
        this.last = commentPage.isLast();
        this.prev = commentPage.hasPrevious();
        this.next = commentPage.hasNext();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentItem {

        private Long commentId;
        private String writer;
        private String content;
        private LocalDateTime createdAt;

        public CommentItem(Comment comment) {
            this.commentId = comment.getId();
            this.writer = comment.getWriter();
            this.content = comment.getContent();
            this.createdAt = comment.getCreatedAt();
        }

    }

}
