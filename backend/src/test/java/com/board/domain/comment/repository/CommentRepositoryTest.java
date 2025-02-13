package com.board.domain.comment.repository;

import com.board.domain.comment.entity.Comment;
import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
import com.board.domain.post.repository.PostRepository;
import com.board.global.common.config.JpaAuditingConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class CommentRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Member saveMember;
    private Post savePost;

    @BeforeEach
    void setUp() {
        saveMember = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
        savePost = postRepository.save(Post.builder()
                .title("title")
                .writer(saveMember.getNickname())
                .content("content")
                .member(saveMember)
                .build());
    }

    @DisplayName("댓글 엔티티를 저장한다.")
    @Test
    void commentSave() {
        Comment comment = Comment.builder()
                .writer(saveMember.getNickname())
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();

        Comment saveComment = commentRepository.save(comment);

        assertThat(saveComment.getId()).isNotNull();
        assertThat(saveComment.getWriter()).isEqualTo("yoonkun");
        assertThat(saveComment.getContent()).isEqualTo("comment");
    }

    @DisplayName("댓글 엔티티 저장 시 writer 필드가 null이면 예외가 발생한다.")
    @Test
    void commentSaveNullWriter() {
        Comment comment = Comment.builder()
                .writer(null)
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();

        assertThatThrownBy(() -> commentRepository.save(comment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("댓글 엔티티 저장 시 content 필드가 null이면 예외가 발생한다.")
    @Test
    void commentSaveNullContent() {
        Comment comment = Comment.builder()
                .writer(saveMember.getNickname())
                .content(null)
                .member(saveMember)
                .post(savePost)
                .build();

        assertThatThrownBy(() -> commentRepository.save(comment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("댓글 엔티티 저장 시 member 필드가 null이면 예외가 발생한다.")
    @Test
    void commentSaveNullMember() {
        Comment comment = Comment.builder()
                .writer(saveMember.getNickname())
                .content("comment")
                .member(null)
                .post(savePost)
                .build();

        assertThatThrownBy(() -> commentRepository.save(comment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("댓글 엔티티 저장 시 post 필드가 null이면 예외가 발생한다.")
    @Test
    void commentSaveNullPost() {
        Comment comment = Comment.builder()
                .writer(saveMember.getNickname())
                .content("comment")
                .member(saveMember)
                .post(null)
                .build();

        assertThatThrownBy(() -> commentRepository.save(comment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}