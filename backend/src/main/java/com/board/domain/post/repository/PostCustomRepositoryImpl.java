package com.board.domain.post.repository;

import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostListResponse.PostItem;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.board.domain.comment.entity.QComment.comment;
import static com.board.domain.post.entity.QPost.post;

@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private static final String TITLE = "title";
    private static final String WRITER = "writer";

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PostListResponse findPosts(Pageable pageable) {
        List<PostItem> content = jpaQueryFactory
                .select(Projections.constructor(PostItem.class,
                        post.id,
                        post.title,
                        post.writer,
                        comment.count().intValue(),
                        post.createdAt)
                )
                .from(post)
                .leftJoin(post.comments, comment).on(comment.isDelete.eq(false))
                .groupBy(post.id)
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> count = jpaQueryFactory
                .select(post.count())
                .from(post);
        Page<PostItem> postPage = PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
        return PostListResponse.of(postPage);
    }

    @Override
    public PostListResponse findSearchPosts(Pageable pageable, String type, String keyword) {
        List<PostItem> content = jpaQueryFactory
                .select(Projections.constructor(PostItem.class,
                        post.id,
                        post.title,
                        post.writer,
                        comment.count().intValue(),
                        post.createdAt)
                )
                .from(post)
                .leftJoin(post.comments, comment).on(comment.isDelete.eq(false))
                .groupBy(post.id)
                .having(contanins(type, keyword))
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> count = jpaQueryFactory
                .select(post.count())
                .where(contanins(type, keyword))
                .from(post);
        Page<PostItem> postPage = PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
        return PostListResponse.of(postPage);
    }

    private BooleanExpression contanins(String type, String keyword) {
        if (type.equals(TITLE)) {
            return post.title.contains(keyword);
        } else if (type.equals(WRITER)) {
            return post.writer.eq(keyword);
        }
        return null;
    }

}
