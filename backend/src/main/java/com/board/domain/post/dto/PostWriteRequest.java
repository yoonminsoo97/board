package com.board.domain.post.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostWriteRequest {

    @NotBlank(message = "제목을 입력해 주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    private String content;

}
