package com.backend.domain.comment.dto;

import com.backend.domain.comment.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

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
        this.comments = commentPage.getContent().stream()
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

}
