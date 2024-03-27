package com.board.domain.comment.dto;

import com.board.domain.comment.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CommentListResponse {

    private final List<CommentListItem> comments;
    private final int pageNumber;
    private final int totalPages;
    private final long totalElements;
    private final boolean prev;
    private final boolean next;
    private final boolean first;
    private final boolean last;

    public CommentListResponse(Page<Comment> commentPage) {
        this.comments = commentPage.getContent().stream()
                .map(CommentListItem::new)
                .collect(Collectors.toList());
        this.pageNumber = commentPage.getNumber() + 1;
        this.totalPages = commentPage.getTotalPages();
        this.totalElements = commentPage.getTotalElements();
        this.prev = commentPage.hasPrevious();
        this.next = commentPage.hasNext();
        this.first = commentPage.isFirst();
        this.last = commentPage.isLast();
    }

}
