package com.board.domain.comment.repository;

import com.board.domain.member.dto.MemberCommentListResponse;

import org.springframework.data.domain.Pageable;

public interface CommentCustomRepository {

    MemberCommentListResponse findMemberCommentList(Pageable pageable, Long memberId);

}
