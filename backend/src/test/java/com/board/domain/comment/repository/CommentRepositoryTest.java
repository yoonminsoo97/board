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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

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

    @DisplayName("게시글 기본 키와 댓글 기본 키로 댓글 엔티티를 조회한다.")
    @Test
    void commentFindByPostIdAndId() {
        Comment comment = Comment.builder()
                .writer(saveMember.getNickname())
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();
        Comment saveComment = commentRepository.save(comment);

        Optional<Comment> findComment = commentRepository.findByPostIdAndId(savePost.getId(), saveComment.getId());

        assertThat(findComment).isNotEmpty();
        assertThat(findComment.get().getWriter()).isEqualTo("yoonkun");
        assertThat(findComment.get().getContent()).isEqualTo("comment");
    }

    @DisplayName("게시글 기본 키와 댓글 기본 키에 해당하는 댓글 엔티티가 존재하지 않으면 빈 Optional 객체를 반환한다.")
    @Test
    void commentFindByPostIdAndIdNotFoundComment() {
        Optional<Comment> findComment = commentRepository.findByPostIdAndId(1L, 1L);

        assertThat(findComment).isEmpty();
    }

    @DisplayName("댓글 엔티티를 삭제한다.")
    @Test
    void commentDelete() {
        Comment comment = Comment.builder()
                .writer(saveMember.getNickname())
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();
        Comment saveComment = commentRepository.save(comment);

        Optional<Comment> findComment = commentRepository.findByPostIdAndId(savePost.getId(), saveComment.getId());

        commentRepository.delete(findComment.get());
    }

    @DisplayName("게시글 기본키를 외래키로 가지고 있는 댓글 엔티티 목록을 조회한다.")
    @Test
    void commentFindAllByPostId() {
        Comment comment = Comment.builder()
                .writer(saveMember.getNickname())
                .content("comment")
                .member(saveMember)
                .post(savePost)
                .build();
        commentRepository.save(comment);

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "id");
        Page<Comment> commentPage = commentRepository.findAllByPostId(savePost.getId(), pageable);

        assertThat(commentPage.getContent().size()).isEqualTo(1);
        assertThat(commentPage.getNumber()).isEqualTo(0);
        assertThat(commentPage.getTotalPages()).isEqualTo(1);
        assertThat(commentPage.getTotalElements()).isEqualTo(1);
        assertThat(commentPage.isFirst()).isTrue();
        assertThat(commentPage.isLast()).isTrue();
        assertThat(commentPage.hasPrevious()).isFalse();
        assertThat(commentPage.hasNext()).isFalse();
    }

}