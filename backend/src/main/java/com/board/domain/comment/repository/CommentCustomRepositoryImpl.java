package com.board.domain.comment.repository;

import com.board.domain.member.dto.MemberCommentListResponse;
import com.board.domain.member.dto.MemberCommentListResponse.CommentItem;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.board.domain.comment.entity.QComment.comment;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public MemberCommentListResponse findMemberCommentList(Pageable pageable, Long memberId) {
        List<CommentItem> content = jpaQueryFactory
                .select(Projections.constructor(CommentItem.class,
                        comment.id,
                        comment.writer,
                        comment.count(),
                        comment.createdAt))
                .from(comment)
                .where(comment.member.id.eq(memberId).and(comment.isDelete.eq(false)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> count = jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.member.id.eq(memberId).and(comment.isDelete.eq(false)));
        Page<CommentItem> commentPage = PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
        return MemberCommentListResponse.of(commentPage);
    }

}
