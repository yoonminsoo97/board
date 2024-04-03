package com.board.domain.post.repository;

import com.board.domain.post.dto.PostListItem;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.board.domain.comment.entity.QComment.comment;
import static com.board.domain.post.entity.QPost.post;

import static com.querydsl.core.types.Projections.constructor;

@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<PostListItem> findPosts(Pageable pageable) {
        List<PostListItem> content = jpaQueryFactory
                .select(constructor(PostListItem.class,
                        post.id,
                        post.title,
                        post.writer,
                        comment.count().intValue(),
                        post.createdAt))
                .from(post)
                .leftJoin(post.comments, comment)
                .groupBy(post.id)
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(post.count())
                .from(post);
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

}
