package com.backend.domain.post.repository;

import com.backend.domain.member.entity.Member;
import com.backend.domain.member.repository.MemberRepository;
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


}