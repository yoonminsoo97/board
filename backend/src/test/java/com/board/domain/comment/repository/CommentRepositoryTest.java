package com.board.domain.comment.repository;

import com.board.domain.comment.entity.Comment;
import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
import com.board.domain.post.repository.PostRepository;
import com.board.global.common.config.JpaAuditConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditConfig.class)
class CommentRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
        post = postRepository.save(Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build());
    }

    @Test
    @DisplayName("댓글을 저장한다")
    void commentSave() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        Comment saveComment = commentRepository.save(comment);

        assertThat(saveComment.getId()).isNotNull();
    }

    @Test
    @DisplayName("댓글과 댓글을 작성한 회원을 한 번에 조회한다")
    void findCommentJoinFetchMember() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();
        Comment saveComment = commentRepository.save(comment);

        Comment findComment = commentRepository.findCommentJoinFetchMember(post.getId(), saveComment.getId()).get();

        assertThat(findComment.getContent()).isEqualTo("댓글");
        assertThat(findComment.getMember().getNickname()).isEqualTo("yoonkun");
        assertThat(findComment.getMember().getUsername()).isEqualTo("yoon1234");
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void commentDelete() {
        Comment comment = Comment.builder()
                .content("댓글")
                .member(member)
                .post(post)
                .build();
        Comment saveComment = commentRepository.save(comment);
        Comment findComment = commentRepository.findCommentJoinFetchMember(post.getId(), saveComment.getId()).get();

        commentRepository.delete(findComment);

        assertThat(commentRepository.findCommentJoinFetchMember(post.getId(), saveComment.getId())).isEmpty();
    }

}