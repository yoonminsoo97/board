package com.board.domain.comment.repository;

import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.member.dto.MemberCommentListResponse;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentCustomRepository {

    List<CommentListResponse.CommentItem> findCommentListByPostId(Long postId);
    List<CommentListResponse.CommentItem.ReplyItem> findReplyListByPostId(Long postId);
    MemberCommentListResponse findMemberCommentList(Pageable pageable, Long memberId);

}
