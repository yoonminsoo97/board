package com.board.domain.comment.repository;

import com.board.domain.comment.entity.Comment;
import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
import com.board.domain.post.repository.PostRepository;
import com.board.global.common.config.JpaAuditConfig;
import com.board.support.config.QuerydslConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditConfig.class, QuerydslConfig.class})
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
    @DisplayName("댓글 목록을 조회한다")
    void findCommentsByPostId() {
        List<Comment> commentList = List.of(
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build()
        );
        commentRepository.saveAll(commentList);

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");
        Page<Comment> commentPage = commentRepository.findCommentsByPostId(pageable, post.getId());

        assertThat(commentPage.getNumber()).isEqualTo(0);
        assertThat(commentPage.getTotalElements()).isEqualTo(5);
        assertThat(commentPage.getTotalPages()).isEqualTo(1);
        assertThat(commentPage.hasPrevious()).isFalse();
        assertThat(commentPage.hasNext()).isFalse();
        assertThat(commentPage.isFirst()).isTrue();
        assertThat(commentPage.isLast()).isTrue();
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

        findComment.delete();

        assertThat(findComment.isDelete()).isTrue();
    }

    @Test
    @DisplayName("특정 회원이 작성한 댓글 목록을 조회한다")
    void findCommentsByMemberUsername() {
        List<Comment> commentList = List.of(
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build(),
                Comment.builder().content("댓글").member(member).post(post).build()
        );
        commentRepository.saveAll(commentList);

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");
        Page<Comment> commentPage = commentRepository.findCommentsByMemberUsername(pageable, member.getUsername());

        assertThat(commentPage.getNumber()).isEqualTo(0);
        assertThat(commentPage.getTotalElements()).isEqualTo(5);
        assertThat(commentPage.getTotalPages()).isEqualTo(1);
        assertThat(commentPage.hasPrevious()).isFalse();
        assertThat(commentPage.hasNext()).isFalse();
        assertThat(commentPage.isFirst()).isTrue();
        assertThat(commentPage.isLast()).isTrue();
    }

}