package com.backend.domain.post.repository;

import com.backend.domain.member.entity.Member;
import com.backend.domain.member.repository.MemberRepository;
import com.backend.domain.post.dto.PostItem;
import com.backend.domain.post.entity.Post;
import com.backend.global.common.config.JpaAuditingConfig;

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

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class PostRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private static Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
    }

    @DisplayName("게시글을 저장한다.")
    @Test
    void postSave() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        Post savePost = postRepository.save(post);

        assertThat(savePost.getId()).isNotNull();
    }

    @DisplayName("게시글 저장 시 필드가 null이면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("nullFieldsPost")
    void postSaveNullFields(Post post) {
        assertThatThrownBy(() -> postRepository.save(post))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Stream<Object> nullFieldsPost() {
        return Stream.of(
                Arguments.of(Named.of("title 필드 null", new Post(null, "writer", "content", member))),
                Arguments.of(Named.of("writer 필드 null", new Post("title", null, "content", member))),
                Arguments.of(Named.of("content 필드 null", new Post("title", "writer", null, member))),
                Arguments.of(Named.of("member 필드 null", new Post("title", "writer", "content", null)))
        );
    }

    @DisplayName("기본키로 게시글을 조회한다.")
    @Test
    void postFindById() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
        Post savePost = postRepository.save(post);

        Optional<Post> findPost = postRepository.findById(savePost.getId());

        assertThat(findPost).isNotEmpty();
        assertThat(findPost.get().getTitle()).isEqualTo("title");
        assertThat(findPost.get().getWriter()).isEqualTo("yoonkun");
        assertThat(findPost.get().getContent()).isEqualTo("content");
    }

    @DisplayName("게시글을 삭제한다.")
    @Test
    void postDelete() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
        Post savePost = postRepository.save(post);

        Post findPost = postRepository.findById(savePost.getId()).get();

        postRepository.delete(findPost);
    }

    @DisplayName("댓글 개수가 포함된 게시글 목록을 PostItem DTO 형식으로 조회한다.")
    @Test
    void postFindAllPostWithCommentCount() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
        postRepository.save(post);

        Page<PostItem> postPage = postRepository.findAllPost(PageRequest.of(0, 10));

        assertThat(postPage.getContent()).isNotNull();
        assertThat(postPage.getNumber()).isEqualTo(0);
        assertThat(postPage.getTotalElements()).isEqualTo(1);
        assertThat(postPage.getTotalPages()).isEqualTo(1);
        assertThat(postPage.isFirst()).isTrue();
        assertThat(postPage.isLast()).isTrue();
        assertThat(postPage.hasPrevious()).isFalse();
        assertThat(postPage.hasNext()).isFalse();
    }

    @DisplayName("제목으로 게시글 목록을 검색한다.")
    @Test
    void postFindAllPostByTitle() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
        postRepository.save(post);

        Page<PostItem> postPage = postRepository.findAllPostByTitle("ti", PageRequest.of(0, 10));

        assertThat(postPage.getContent()).isNotNull();
        assertThat(postPage.getNumber()).isEqualTo(0);
        assertThat(postPage.getTotalElements()).isEqualTo(1);
        assertThat(postPage.getTotalPages()).isEqualTo(1);
        assertThat(postPage.isFirst()).isTrue();
        assertThat(postPage.isLast()).isTrue();
        assertThat(postPage.hasPrevious()).isFalse();
        assertThat(postPage.hasNext()).isFalse();
    }

    @DisplayName("작성자로 게시글 목록을 검색한다.")
    @Test
    void postFindAllPostByWriter() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
        postRepository.save(post);

        Page<PostItem> postPage = postRepository.findAllPostByWriter("yo", PageRequest.of(0, 10));

        assertThat(postPage.getContent()).isNotNull();
        assertThat(postPage.getNumber()).isEqualTo(0);
        assertThat(postPage.getTotalElements()).isEqualTo(1);
        assertThat(postPage.getTotalPages()).isEqualTo(1);
        assertThat(postPage.isFirst()).isTrue();
        assertThat(postPage.isLast()).isTrue();
        assertThat(postPage.hasPrevious()).isFalse();
        assertThat(postPage.hasNext()).isFalse();
    }

}