package com.board.domain.reply.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.post.entity.Post;
import com.board.domain.comment.entity.Comment;
import com.board.domain.comment.repository.CommentRepository;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.repository.PostRepository;

import com.board.domain.reply.entity.Reply;
import com.board.global.common.config.JpaAuditConfig;
import com.board.support.config.QuerydslConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditConfig.class})
class ReplyRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    private Member member;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                Member.builder()
                        .nickname("yoonkun")
                        .username("yoon1234")
                        .password("12345678")
                        .build()
        );
        post = postRepository.save(
                Post.builder()
                        .title("title")
                        .content("content")
                        .member(member)
                        .build()
        );
        comment = commentRepository.save(
                Comment.builder()
                        .content("comment")
                        .member(member)
                        .post(post)
                        .build()
        );
    }

    @Test
    @DisplayName("대댓글을 저장한다")
    void replySave() {
        Reply reply = Reply.builder()
                .content("reply")
                .member(member)
                .comment(comment)
                .build();

        Reply saveReply = replyRepository.save(reply);

        assertThat(saveReply.getId()).isNotNull();
    }

}