package com.backend.domain.comment.repository;

import com.backend.domain.comment.entity.Comment;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.repository.MemberRepository;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.repository.PostRepository;
import com.backend.global.common.config.JpaAuditingConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class CommentRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private static Member saveMember;
    private static Post savePost;

    @BeforeEach
    void setUp() {
        saveMember = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
        savePost = postRepository.save(Post.builder()
                .title("title")
                .writer("yoonkun")
                .content("content")
                .member(saveMember)
                .build());
    }

    @DisplayName("댓글을 저장한다.")
    @Test
    void commentSave() {
        Comment comment = Comment.builder()
                .writer("yoonkun")
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();

        Comment saveComment = commentRepository.save(comment);

        assertThat(saveComment.getId()).isNotNull();
    }

    @DisplayName("댓글 저장 시 필드가 null이면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("nullFieldsComment")
    void commentSaveNullFields(Comment comment) {
        assertThatThrownBy(() -> commentRepository.save(comment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Stream<Object> nullFieldsComment() {
        return Stream.of(
                Arguments.of(Named.of("writer 필드 null", new Comment(null, "comment", saveMember, savePost))),
                Arguments.of(Named.of("content 필드 null", new Comment("yoonkun", null, saveMember, savePost))),
                Arguments.of(Named.of("member 필드 null", new Comment("yoonkun", "comment", null, savePost))),
                Arguments.of(Named.of("post 필드 null", new Comment("yoonkun", "comment", saveMember, null)))
        );
    }

    @DisplayName("게시글 기본키와 댓글 기본키로 댓글을 조회한다.")
    @Test
    void commentFindByPostIdAndCommentId() {
        Comment comment = Comment.builder()
                .writer("yoonkun")
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();
        Comment saveComment = commentRepository.save(comment);

        Optional<Comment> findComment = commentRepository.findByPostIdAndCommentId(savePost.getId(), saveComment.getId());

        assertThat(findComment).isPresent();
        assertThat(findComment.get().getWriter()).isEqualTo("yoonkun");
        assertThat(findComment.get().getContent()).isEqualTo("comment");
    }

    @DisplayName("댓글을 삭제한다.")
    @Test
    void commentDelete() {
        Comment comment = Comment.builder()
                .writer("yoonkun")
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();
        Comment saveComment = commentRepository.save(comment);

        Optional<Comment> findComment = commentRepository.findByPostIdAndCommentId(savePost.getId(), saveComment.getId());

        commentRepository.delete(findComment.get());
    }

    @DisplayName("게시글 기본키를 외래키로 가지고 있는 댓글을 전부 삭제한다.")
    @Test
    void commentDeleteByPostId() {
        Comment comment = Comment.builder()
                .writer("yoonkun")
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();
        commentRepository.save(comment);

        commentRepository.deleteByPostId(savePost.getId());
    }

    @DisplayName("게시글 기본키를 외래키로 가지고 있는 댓글 목록을 조회한다.")
    @Test
    void commentFindAllByPostId() {
        Comment comment = Comment.builder()
                .writer("yoonkun")
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();
        commentRepository.save(comment);

        Page<Comment> commentPage = commentRepository.findAllByPostId(savePost.getId(), PageRequest.of(0, 10, Sort.Direction.ASC, "id"));

        assertThat(commentPage.getContent()).isNotEmpty();
        assertThat(commentPage.getNumber()).isEqualTo(0);
        assertThat(commentPage.getTotalPages()).isEqualTo(1);
        assertThat(commentPage.getTotalElements()).isEqualTo(1);
        assertThat(commentPage.isFirst()).isTrue();
        assertThat(commentPage.isLast()).isTrue();
        assertThat(commentPage.hasPrevious()).isFalse();
        assertThat(commentPage.hasNext()).isFalse();
    }

}