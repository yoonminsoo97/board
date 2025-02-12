package com.board.domain.post.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
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
class PostRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private Member saveMember;

    @BeforeEach
    void setUp() {
        saveMember = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
    }

    @DisplayName("게시글 엔티티를 저장한다.")
    @Test
    void postSave() {
        Post post = Post.builder()
                .title("title")
                .writer(saveMember.getNickname())
                .content("content")
                .member(saveMember)
                .build();

        Post savePost = postRepository.save(post);

        assertThat(savePost.getId()).isNotNull();
        assertThat(savePost.getTitle()).isEqualTo("title");
        assertThat(savePost.getWriter()).isEqualTo("yoonkun");
        assertThat(savePost.getContent()).isEqualTo("content");
    }

    @DisplayName("게시글 엔티티 저장 시 title 필드가 null 이면 예외가 발생한다.")
    @Test
    void postSaveNullTitle() {
        Post post = Post.builder()
                .title(null)
                .writer(saveMember.getNickname())
                .content("content")
                .member(saveMember)
                .build();

        assertThatThrownBy(() -> postRepository.save(post))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("게시글 엔티티 저장 시 writer 필드가 null 이면 예외가 발생한다.")
    @Test
    void postSaveNullWriter() {
        Post post = Post.builder()
                .title("title")
                .writer(null)
                .content("content")
                .member(saveMember)
                .build();

        assertThatThrownBy(() -> postRepository.save(post))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("게시글 엔티티 저장 시 content 필드가 null 이면 예외가 발생한다.")
    @Test
    void postSaveNullContent() {
        Post post = Post.builder()
                .title("title")
                .writer(saveMember.getNickname())
                .content(null)
                .member(saveMember)
                .build();

        assertThatThrownBy(() -> postRepository.save(post))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("게시글 엔티티 저장 시 member 필드가 null 이면 예외가 발생한다.")
    @Test
    void postSaveNullMember() {
        Post post = Post.builder()
                .title("title")
                .writer(saveMember.getNickname())
                .content("content")
                .member(null)
                .build();

        assertThatThrownBy(() -> postRepository.save(post))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("게시글 기본키로 게시글 엔티티를 조회한다.")
    @Test
    void postFindById() {
        Post post = Post.builder()
                .title("title")
                .writer(saveMember.getNickname())
                .content("content")
                .member(saveMember)
                .build();
        Post savePost = postRepository.save(post);

        Optional<Post> findPost = postRepository.findById(savePost.getId());

        assertThat(findPost).isNotEmpty();
        assertThat(findPost.get().getTitle()).isEqualTo("title");
        assertThat(findPost.get().getWriter()).isEqualTo("yoonkun");
        assertThat(findPost.get().getContent()).isEqualTo("content");
    }

    @DisplayName("게시글 기본키에 해당하는 게시글이 존재하지 않으면 빈 Optional 객체를 반환한다.")
    @Test
    void postFindByIdNotFoundPost() {
        Optional<Post> findPost = postRepository.findById(1L);

        assertThat(findPost).isEmpty();
    }

    @DisplayName("게시글 엔티티를 삭제한다.")
    @Test
    void postDelete() {
        Post post = Post.builder()
                .title("title")
                .writer(saveMember.getNickname())
                .content("content")
                .member(saveMember)
                .build();
        Post savePost = postRepository.save(post);
        Post findPost = postRepository.findById(savePost.getId()).get();

        postRepository.delete(findPost);
    }

    @DisplayName("게시글 엔티티 목록을 조회한다.")
    @Test
    void postFindAll() {
        Post post = Post.builder()
                .title("title")
                .writer(saveMember.getNickname())
                .content("content")
                .member(saveMember)
                .build();
        postRepository.save(post);

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "id");
        Page<Post> postPage = postRepository.findAll(pageable);

        assertThat(postPage.getNumber()).isEqualTo(0);
        assertThat(postPage.getTotalPages()).isEqualTo(1);
        assertThat(postPage.getTotalElements()).isEqualTo(1);
        assertThat(postPage.isFirst()).isTrue();
        assertThat(postPage.isLast()).isTrue();
        assertThat(postPage.hasPrevious()).isFalse();
        assertThat(postPage.hasNext()).isFalse();
    }

}