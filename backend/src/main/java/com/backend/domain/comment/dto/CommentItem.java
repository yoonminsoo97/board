package com.backend.domain.comment.dto;

import com.backend.domain.comment.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentItem {

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
