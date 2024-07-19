package com.board.domain.post.service;

import com.board.domain.comment.repository.CommentRepository;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostListResponse.PostItem;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.domain.post.exception.PostDeleteAccessDeniedException;
import com.board.domain.post.exception.PostModifyAccessDeniedException;
import com.board.domain.post.repository.PostRepository;
import com.board.support.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

class PostServiceTest extends ServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Nested
    @DisplayName("게시글 작성")
    class PostWriteTest {

        @Test
        @DisplayName("게시글을 작성한다")
        void postWrite() {
            PostWriteRequest postWriteRequest = PostWriteRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            given(memberRepository.findByMemberId(anyLong())).willReturn(member);
            given(postRepository.save(any(Post.class))).willReturn(post);

            postService.postWrite(postWriteRequest, 1L);

            then(memberRepository).should().findByMemberId(anyLong());
            then(postRepository).should().save(any(Post.class));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
        void postWriteNotFoundMember() {
            PostWriteRequest postWriteRequest = PostWriteRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            willThrow(new NotFoundMemberException()).given(memberRepository).findByMemberId(anyLong());

            assertThatThrownBy(() -> postService.postWrite(postWriteRequest, 1L))
                    .isInstanceOf(NotFoundMemberException.class);

            then(memberRepository).should().findByMemberId(anyLong());
            then(postRepository).should(never()).save(any(Post.class));
        }

    }

    @Nested
    @DisplayName("게시글 상세조회")
    class PostDetailTest {

        @Test
        @DisplayName("게시글을 상세조회 한다")
        void postDetail() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            given(postRepository.findByPostId(anyLong())).willReturn(post);

            PostDetailResponse response = postService.postDetail(1L);

            assertThat(response.getTitle()).isEqualTo("title");
            assertThat(response.getWriter()).isEqualTo("yoonkun");
            assertThat(response.getContent()).isEqualTo("content");
            then(postRepository).should().findByPostId(anyLong());
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void postDetailNotFoundMember() {
            willThrow(new NotFoundPostException()).given(postRepository).findByPostId(anyLong());

            assertThatThrownBy(() -> postService.postDetail(1L))
                    .isInstanceOf(NotFoundPostException.class);

            then(postRepository).should().findByPostId(anyLong());
        }

    }

    @Nested
    @DisplayName("게시글 목록조회")
    class PostListTest {

        @Test
        @DisplayName("게시글 목록을 조회한다")
        void findPostList() {
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(List.of(
                            PostItem.builder()
                                    .postId(1L)
                                    .title("title")
                                    .writer("writer")
                                    .commentCount(0)
                                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                                    .build()
                    ))
                    .page(1)
                    .totalPages(1)
                    .totalElements(1)
                    .first(true)
                    .last(true)
                    .prev(false)
                    .next(false)
                    .build();

            given(postRepository.findPostList(any(Pageable.class))).willReturn(postListResponse);

            PostListResponse response = postService.postList(0);

            assertThat(response.getPosts().get(0).getPostId()).isEqualTo(1L);
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isPrev()).isFalse();
            assertThat(response.isNext()).isFalse();
            then(postRepository).should().findPostList(any(Pageable.class));
        }

        @Test
        @DisplayName("검색 조건에 맞는 게시글 목록을 조회한다")
        void findSearchPostList() {
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(List.of(
                            PostItem.builder()
                                    .postId(1L)
                                    .title("hello")
                                    .writer("writer")
                                    .commentCount(0)
                                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                                    .build()
                    ))
                    .page(1)
                    .totalPages(1)
                    .totalElements(1)
                    .first(true)
                    .last(true)
                    .prev(false)
                    .next(false)
                    .build();

            given(postRepository.findPostSearchList(any(Pageable.class), anyString(), anyString())).willReturn(postListResponse);

            PostListResponse response = postService.postSearchList(0, "title", "hello");

            assertThat(response.getPosts().get(0).getTitle()).isEqualTo("hello");
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isPrev()).isFalse();
            assertThat(response.isNext()).isFalse();
            then(postRepository).should().findPostSearchList(any(Pageable.class), anyString(), anyString());
        }

    }

    @Nested
    @DisplayName("게시글 수정")
    class PostModifyTest {

        @Test
        @DisplayName("게시글을 수정한다")
        void postModify() {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("newTitle")
                    .content("newContent")
                    .build();

            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            given(postRepository.findByPostId(anyLong())).willReturn(post);

            postService.postModify(1L, postModifyRequest, 1L);

            then(postRepository).should().findByPostId(anyLong());
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void postModifyNotFoundPost() {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("newTitle")
                    .content("newContent")
                    .build();

            willThrow(new NotFoundPostException()).given(postRepository).findByPostId(anyLong());

            assertThatThrownBy(() -> postService.postModify(1L, postModifyRequest, 1L))
                    .isInstanceOf(NotFoundPostException.class);

            then(postRepository).should().findByPostId(anyLong());
        }

        @Test
        @DisplayName("작성자가 아닌데 수정을 시도하면 예외가 발생한다")
        void postModifyNotPostOwner() {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("newTitle")
                    .content("newContent")
                    .build();

            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            given(postRepository.findByPostId(anyLong())).willReturn(post);

            assertThatThrownBy(() -> postService.postModify(1L, postModifyRequest, 2L))
                    .isInstanceOf(PostModifyAccessDeniedException.class);

            then(postRepository).should().findByPostId(anyLong());
        }


    }

    @Nested
    @DisplayName("게시글 삭제")
    class PostDeleteTest {

        @Test
        @DisplayName("게시글을 삭제한다")
        void postDelete() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            given(postRepository.findByPostId(anyLong())).willReturn(post);
            willDoNothing().given(commentRepository).deleteByPostId(anyLong());
            willDoNothing().given(postRepository).delete(any(Post.class));

            postService.postDelete(1L, 1L);

            then(postRepository).should().findByPostId(anyLong());
            then(commentRepository).should().deleteByPostId(anyLong());
            then(postRepository).should().delete(any(Post.class));
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void postDeleteNotFoundPost() {
            willThrow(new NotFoundPostException()).given(postRepository).findByPostId(anyLong());

            assertThatThrownBy(() -> postService.postDelete(1L, 1L))
                    .isInstanceOf(NotFoundPostException.class);

            then(postRepository).should().findByPostId(anyLong());
            then(commentRepository).should(never()).deleteByPostId(anyLong());
            then(postRepository).should(never()).delete(any(Post.class));
        }

        @Test
        @DisplayName("작성자가 아닌데 삭제를 시도하면 예외가 발생한다")
        void postDeleteNotPostOwner() {
            Post post = Post.builder()
                    .title("title")
                    .writer(member.getNickname())
                    .content("content")
                    .member(member)
                    .build();

            given(postRepository.findByPostId(anyLong())).willReturn(post);

            assertThatThrownBy(() -> postService.postDelete(1L, 2L))
                    .isInstanceOf(PostDeleteAccessDeniedException.class);

            then(postRepository).should().findByPostId(anyLong());
            then(commentRepository).should(never()).deleteByPostId(anyLong());
            then(postRepository).should(never()).delete(any(Post.class));
        }

    }

}