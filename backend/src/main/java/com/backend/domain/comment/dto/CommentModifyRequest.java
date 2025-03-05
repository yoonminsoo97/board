package com.backend.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentModifyRequest {

    @NotBlank(message = "내용을 입력해 주세요.")
    private String content;

}
