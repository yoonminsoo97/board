package com.board.domain.comment.dto;

import com.board.domain.comment.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentListItem {

    private final Long commentId;
    private final String writer;
    private final String content;
    private final LocalDateTime createdAt;

    public CommentListItem(Comment comment) {
        this.commentId = comment.getId();
        this.writer = comment.getWriter();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }

}
