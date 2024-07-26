package com.board.domain.comment.repository;

import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.member.dto.MemberCommentListResponse;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.board.domain.comment.entity.QComment.comment;
import static com.querydsl.core.types.Projections.constructor;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CommentListResponse.CommentItem> findCommentListByPostId(Long postId) {
        return jpaQueryFactory
                .select(constructor(CommentListResponse.CommentItem.class,
                        comment.id,
                        comment.writer,
                        comment.content,
                        comment.createdAt,
                        comment.isDelete))
                .from(comment)
                .where(comment.post.id.eq(postId).and(comment.reference.isNull()))
                .orderBy(comment.id.asc())
                .fetch();
    }

    @Override
    public List<CommentListResponse.CommentItem.ReplyItem> findReplyListByPostId(Long postId) {
        return jpaQueryFactory
                .select(constructor(CommentListResponse.CommentItem.ReplyItem.class,
                        comment.id,
                        comment.reference.id,
                        comment.writer,
                        comment.content,
                        comment.createdAt,
                        comment.isDelete))
                .from(comment)
                .where(comment.post.id.eq(postId).and(comment.reference.isNull()))
                .orderBy(comment.reference.id.asc())
                .fetch();
    }

    @Override
    public MemberCommentListResponse findMemberCommentList(Pageable pageable, Long memberId) {
        List<MemberCommentListResponse.CommentItem> content = jpaQueryFactory
                .select(constructor(MemberCommentListResponse.CommentItem.class,
                        comment.id,
                        comment.writer,
                        comment.content,
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
        Page<MemberCommentListResponse.CommentItem> commentPage = PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
        return MemberCommentListResponse.of(commentPage);
    }

}
