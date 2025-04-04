package com.backend.domain.post.service;

import com.backend.domain.comment.repository.CommentRepository;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.exception.NotFoundMemberException;
import com.backend.domain.member.repository.MemberRepository;
import com.backend.domain.post.dto.PostDetailResponse;
import com.backend.domain.post.dto.PostItem;
import com.backend.domain.post.dto.PostListResponse;
import com.backend.domain.post.dto.PostModifyRequest;
import com.backend.domain.post.dto.PostWriteRequest;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.exception.AccessDeniedDeletePostException;
import com.backend.domain.post.exception.AccessDeniedModifyPostException;
import com.backend.domain.post.exception.NotFoundPostException;
import com.backend.domain.post.repository.PostRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

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
                .password("12345678")
                .build();
    }

    @DisplayName("게시글을 작성한다.")
    @Test
    void postWrite() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(postRepository.save(any(Post.class))).willReturn(post);

        postService.postWrite(postWriteRequest, "yoon1234");

        then(memberRepository).should().findByUsername(anyString());
        then(postRepository).should().save(any(Post.class));
    }

    @DisplayName("게시글 작성 시 회원이 존재하지 않으면 예외가 발생한다.")
    @Test
    void postWriteNotFoundMember() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");

        willThrow(new NotFoundMemberException()).given(memberRepository).findByUsername(anyString());

        assertThatThrownBy(() -> postService.postWrite(postWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundMemberException.class);

        then(memberRepository).should().findByUsername(anyString());
        then(postRepository).should(never()).save(any(Post.class));
    }

    @DisplayName("게시글을 상세조회 한다.")
    @Test
    void postDetail() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        PostDetailResponse postDetailResponse = postService.postDetail(1L);

        assertThat(postDetailResponse.getTitle()).isEqualTo("title");
        assertThat(postDetailResponse.getWriter()).isEqualTo("yoonkun");
        assertThat(postDetailResponse.getContent()).isEqualTo("content");
        then(postRepository).should().findById(anyLong());
    }

    @DisplayName("게시글 상세조회 시 게시글이 존재하지 않으면 예외가 발생한다.")
    @Test
    void postDetailNotFoundPost() {
        willThrow(new NotFoundPostException()).given(postRepository).findById(anyLong());

        assertThatThrownBy(() -> postService.postDetail(1L))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findById(anyLong());
    }

    @DisplayName("게시글 목록을 조회한다.")
    @Test
    void postList() {
        List<PostItem> content = List.of(
                new PostItem(1L, "title", "writer", LocalDateTime.now(), 5)
        );
        PageImpl<PostItem> postPage = new PageImpl<>(content);

        given(postRepository.findAllPost(any(Pageable.class))).willReturn(postPage);

        PostListResponse postListResponse = postService.postList(1);

        assertThat(postListResponse.getPosts()).isNotNull();
        assertThat(postListResponse.getPage()).isEqualTo(1);
        assertThat(postListResponse.getTotalPosts()).isEqualTo(1);
        assertThat(postListResponse.getTotalPages()).isEqualTo(1);
        assertThat(postListResponse.isFirst()).isTrue();
        assertThat(postListResponse.isLast()).isTrue();
        assertThat(postListResponse.isPrev()).isFalse();
        assertThat(postListResponse.isNext()).isFalse();
        then(postRepository).should().findAllPost(any(Pageable.class));
    }

    @DisplayName("게시글을 검색한다.")
    @Test
    void postListSearch() {
        List<PostItem> content = List.of(
                new PostItem(1L, "title", "writer", LocalDateTime.now(), 5)
        );
        PageImpl<PostItem> postPage = new PageImpl<>(content);

        given(postRepository.findAllPostByTitle(anyString(), any(Pageable.class))).willReturn(postPage);

        PostListResponse postListResponse = postService.postListSearch("title", "ti", 1);

        assertThat(postListResponse.getPosts()).isNotNull();
        assertThat(postListResponse.getPage()).isEqualTo(1);
        assertThat(postListResponse.getTotalPosts()).isEqualTo(1);
        assertThat(postListResponse.getTotalPages()).isEqualTo(1);
        assertThat(postListResponse.isFirst()).isTrue();
        assertThat(postListResponse.isLast()).isTrue();
        assertThat(postListResponse.isPrev()).isFalse();
        assertThat(postListResponse.isNext()).isFalse();
        then(postRepository).should().findAllPostByTitle(anyString(), any(Pageable.class));
    }

    @DisplayName("게시글을 수정한다.")
    @Test
    void postModify() {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        postService.postModify(1L, "yoon1234", postModifyRequest);

        then(postRepository).should().findById(anyLong());
    }

    @DisplayName("게시글 수정 시 작성자가 아닌데 수정을 시도할 경우 예외가 발생한다.")
    @Test
    void postModifyAccessDenied() {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        String notPostOwner = "yoonyoon";
        assertThatThrownBy(() -> postService.postModify(1L, notPostOwner, postModifyRequest))
                .isInstanceOf(AccessDeniedModifyPostException.class);

        then(postRepository).should().findById(anyLong());
    }

    @DisplayName("게시글 수정 시 게시글이 존재하지 않으면 예외가 발생한다.")
    @Test
    void postModifyNotFoundPost() {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");

        willThrow(new NotFoundPostException()).given(postRepository).findById(anyLong());

        assertThatThrownBy(() -> postService.postModify(1L, "yoon1234", postModifyRequest))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findById(anyLong());
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

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        willDoNothing().given(commentRepository).deleteByPostId(anyLong());
        willDoNothing().given(postRepository).delete(any(Post.class));

        postService.postDelete(1L, "yoon1234");

        then(postRepository).should().findById(anyLong());
        then(commentRepository).should().deleteByPostId(anyLong());
        then(postRepository).should().delete(any(Post.class));
    }

    @DisplayName("게시글 삭제 시 작성자가 아닌데 삭제를 시도할 경우 예외가 발생한다.")
    @Test
    void postDeleteAccessDenied() {
        Post post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        String notPostOwner = "yoonyoon";
        assertThatThrownBy(() -> postService.postDelete(1L, notPostOwner))
                .isInstanceOf(AccessDeniedDeletePostException.class);

        then(postRepository).should().findById(anyLong());
        then(commentRepository).should(never()).deleteByPostId(anyLong());
        then(postRepository).should(never()).delete(any(Post.class));
    }

    @DisplayName("게시글 삭제 시 게시글이 존재하지 않으면 예외가 발생한다.")
    @Test
    void postDeleteNotFoundPost() {
        willThrow(new NotFoundPostException()).given(postRepository).findById(anyLong());

        assertThatThrownBy(() -> postService.postDelete(1L, "yoon1234"))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findById(anyLong());
        then(postRepository).should(never()).delete(any(Post.class));
    }

}