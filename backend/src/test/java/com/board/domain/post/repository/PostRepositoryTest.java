package com.board.domain.post.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostListItem;
import com.board.domain.post.entity.Post;
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
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
    }

    @Test
    @DisplayName("게시글을 저장한다")
    void postSave() {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        Post savePost = postRepository.save(post);

        assertThat(savePost.getId()).isNotNull();
    }

    @Test
    @DisplayName("게시글 기본키로 상세조회 한다")
    void postDetail() {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        Post savePost = postRepository.save(post);

        Post findPost = postRepository.findById(savePost.getId()).get();

        assertThat(findPost.getTitle()).isEqualTo("제목");
        assertThat(findPost.getContent()).isEqualTo("내용");
    }

    @Test
    @DisplayName("게시글과 해당 게시글을 작성한 회원을 한 번에 조회한다")
    void findPostJoinFetch() {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        Post savePost = postRepository.save(post);

        Post findPost = postRepository.findPostJoinFetch(savePost.getId()).get();

        assertThat(findPost.getTitle()).isEqualTo("제목");
        assertThat(findPost.getContent()).isEqualTo("내용");
        assertThat(findPost.getMember().getNickname()).isEqualTo("yoonkun");
        assertThat(findPost.getMember().getUsername()).isEqualTo("yoon1234");
    }

    @Test
    @DisplayName("게시글 목록을 조회한다")
    void postFindAll() {
        List<Post> content = List.of(
                Post.builder().title("제목").content("내용").member(member).build(),
                Post.builder().title("제목").content("내용").member(member).build(),
                Post.builder().title("제목").content("내용").member(member).build(),
                Post.builder().title("제목").content("내용").member(member).build(),
                Post.builder().title("제목").content("내용").member(member).build()
        );
        postRepository.saveAll(content);

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "id");
        Page<PostListItem> postPage = postRepository.findPosts(pageable);

        assertThat(postPage.getNumber()).isEqualTo(0);
        assertThat(postPage.getTotalPages()).isEqualTo(1);
        assertThat(postPage.getTotalElements()).isEqualTo(5);
        assertThat(postPage.hasPrevious()).isFalse();
        assertThat(postPage.hasNext()).isFalse();
        assertThat(postPage.isFirst()).isTrue();
        assertThat(postPage.isLast()).isTrue();
    }

    @Test
    @DisplayName("특정 단어가 포함된 게시글 목록을 조회한다")
    void findPostsSearch() {
        List<Post> content = List.of(
                Post.builder().title("독수리").content("내용").member(member).build(),
                Post.builder().title("기러기").content("내용").member(member).build(),
                Post.builder().title("호랑이").content("내용").member(member).build(),
                Post.builder().title("거북이").content("내용").member(member).build(),
                Post.builder().title("코끼리").content("내용").member(member).build()
        );
        postRepository.saveAll(content);

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "id");
        Page<PostListItem> postPage = postRepository.findPostsSearch(pageable, "title", "이");

        assertThat(postPage.getNumber()).isEqualTo(0);
        assertThat(postPage.getTotalPages()).isEqualTo(1);
        assertThat(postPage.getTotalElements()).isEqualTo(2);
        assertThat(postPage.hasPrevious()).isFalse();
        assertThat(postPage.hasNext()).isFalse();
        assertThat(postPage.isFirst()).isTrue();
        assertThat(postPage.isLast()).isTrue();
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void postDelete() {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        Post savePost = postRepository.save(post);
        Post findPost = postRepository.findPostJoinFetch(savePost.getId()).get();

        postRepository.delete(findPost);

        assertThat(postRepository.findPostJoinFetch(savePost.getId())).isEmpty();
    }

}