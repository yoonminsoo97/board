package com.board.domain.post.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.entity.Post;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.support.RepositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostRepositoryTest extends RepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private Member member;
    private Member memberB;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        memberB = Member.builder()
                .nickname("yoongun")
                .username("yoon5678")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        memberRepository.save(member);
        memberRepository.save(memberB);
    }

    @Nested
    @DisplayName("게시글 저장")
    class PostSaveTest {

        @Test
        @DisplayName("게시글을 저장한다")
        void save() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            Post savePost = postRepository.save(post);

            assertThat(savePost.getId()).isNotNull();
        }

        @Test
        @DisplayName("제목이 Null이면 예외가 발생한다")
        void saveNullTitle() {
            Post post = Post.builder()
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            assertThatThrownBy(() -> postRepository.save(post))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("작성자가 Null이면 예외가 발생한다")
        void saveNullWriter() {
            Post post = Post.builder()
                    .title("title")
                    .content("content")
                    .member(member)
                    .build();

            assertThatThrownBy(() -> postRepository.save(post))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("내용이 Null이면 예외가 발생한다")
        void saveNullContent() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .member(member)
                    .build();

            assertThatThrownBy(() -> postRepository.save(post))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("회원이 Null이면 예외가 발생한다")
        void saveNullMember() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .build();

            assertThatThrownBy(() -> postRepository.save(post))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class PostFindTest {

        @Test
        @DisplayName("게시글 기본키(id)로 조회한다")
        void findByPostId() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();
            postRepository.save(post);

            Post findPost = postRepository.findByPostId(post.getId());

            assertThat(findPost.getTitle()).isEqualTo("title");
            assertThat(findPost.getWriter()).isEqualTo("yoonkun");
            assertThat(findPost.getContent()).isEqualTo("content");
        }

        @Test
        @DisplayName("기본키(id)에 해당하는 게시글이 없으면 예외가 발생한다")
        void findByPostIdNotFoundPost() {
            assertThatThrownBy(() -> postRepository.findByPostId(1L))
                    .isInstanceOf(NotFoundPostException.class);
        }

    }

    @Nested
    @DisplayName("게시글 목록 조회")
    @Transactional
    class PostListFindTest {

        @Test
        @DisplayName("게시글 목록을 조회한다")
        void findPostList() {
            postRepository.saveAll(postList());

            PostListResponse postListResponse = postRepository.findPostList(PageRequest.of(0, 10));

            assertThat(postListResponse.getPage()).isEqualTo(1);
            assertThat(postListResponse.getTotalPages()).isEqualTo(1);
            assertThat(postListResponse.getTotalElements()).isEqualTo(4);
            assertThat(postListResponse.isNext()).isFalse();
            assertThat(postListResponse.isPrev()).isFalse();
            assertThat(postListResponse.isFirst()).isTrue();
            assertThat(postListResponse.isLast()).isTrue();
        }

        @Test
        @DisplayName("제목이 apple인 게시글 목록을 조회한다")
        void findSearchTitlePostList() {
            postRepository.saveAll(postList());

            PostListResponse postListResponse = postRepository.findPostSearchList(PageRequest.of(0, 10), "title", "apple");

            assertThat(postListResponse.getPage()).isEqualTo(1);
            assertThat(postListResponse.getTotalPages()).isEqualTo(1);
            assertThat(postListResponse.getTotalElements()).isEqualTo(2);
            assertThat(postListResponse.isNext()).isFalse();
            assertThat(postListResponse.isPrev()).isFalse();
            assertThat(postListResponse.isFirst()).isTrue();
            assertThat(postListResponse.isLast()).isTrue();
        }

        @Test
        @DisplayName("작성자가 yoonkun인 게시글 목록을 조회한다")
        void findSearchWriterPostList() {
            postRepository.saveAll(postList());

            PostListResponse postListResponse = postRepository.findPostSearchList(PageRequest.of(0, 10), "writer", "yoonkun");

            assertThat(postListResponse.getPage()).isEqualTo(1);
            assertThat(postListResponse.getTotalPages()).isEqualTo(1);
            assertThat(postListResponse.getTotalElements()).isEqualTo(2);
            assertThat(postListResponse.isNext()).isFalse();
            assertThat(postListResponse.isPrev()).isFalse();
            assertThat(postListResponse.isFirst()).isTrue();
            assertThat(postListResponse.isLast()).isTrue();
        }

        @Test
        @DisplayName("자신이 작성한 게시글 목록을 조회한다")
        void findmemberPostList() {
            postRepository.saveAll(postList());

            PostListResponse postListResponse = postRepository.findPostMemberList(PageRequest.of(0, 10), member.getId());

            assertThat(postListResponse.getPage()).isEqualTo(1);
            assertThat(postListResponse.getTotalPages()).isEqualTo(1);
            assertThat(postListResponse.getTotalElements()).isEqualTo(2);
            assertThat(postListResponse.isNext()).isFalse();
            assertThat(postListResponse.isPrev()).isFalse();
            assertThat(postListResponse.isFirst()).isTrue();
            assertThat(postListResponse.isLast()).isTrue();
        }

        private List<Post> postList() {
            return List.of(
                    Post.builder().title("apple").writer("yoonkun").content("content").member(member).build(),
                    Post.builder().title("banana").writer("yoongun").content("content").member(memberB).build(),
                    Post.builder().title("apple").writer("yoonkun").content("content").member(member).build(),
                    Post.builder().title("title").writer("yoongun").content("content").member(memberB).build()
            );
        }

    }

    @Nested
    @DisplayName("게시글 삭제")
    class PostDelete {

        @Test
        @DisplayName("게시글을 삭제한다")
        void delete() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();
            postRepository.save(post);

            postRepository.delete(post);

            assertThat(postRepository.findById(post.getId())).isEmpty();
        }

    }

}