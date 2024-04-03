package com.board.domain.post.service;

import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostListItem;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.domain.post.exception.PostDeleteAccessDeniedException;
import com.board.domain.post.exception.PostModifyAccessDeniedException;
import com.board.domain.post.repository.PostRepository;

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

    @InjectMocks
    private PostService postService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .build();
    }

    @Test
    @DisplayName("게시글을 작성한다")
    void postWrite() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");
        Post post = Post.builder()
                .title(postWriteRequest.getTitle())
                .content(postWriteRequest.getContent())
                .member(member)
                .build();

        given(memberRepository.findMemberByUsername(anyString())).willReturn(Optional.of(member));
        given(postRepository.save(any(Post.class))).willReturn(post);

        postService.postWrite(postWriteRequest, "yoon1234");

        then(memberRepository).should().findMemberByUsername(anyString());
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void postWrite_notFoundPost() {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");

        willThrow(new NotFoundMemberException()).given(memberRepository).findMemberByUsername(anyString());

        assertThatThrownBy(() -> postService.postWrite(postWriteRequest, "yoon1234"))
                .isInstanceOf(NotFoundMemberException.class);

        then(memberRepository).should().findMemberByUsername(anyString());
        then(postRepository).should(never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글을 상세조회 한다")
    void postDetail() {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        postService.postDetail(1L);

        then(postRepository).should().findById(anyLong());
    }

    @Test
    @DisplayName("게시글 상세조회 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDetail_notFoundPost() {
        willThrow(new NotFoundPostException()).given(postRepository).findById(anyLong());

        assertThatThrownBy(() -> postService.postDetail(1L))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findById(anyLong());
    }

    @Test
    @DisplayName("게시글 목록을 조회한다")
    void postList() {
        List<PostListItem> content = List.of(
                new PostListItem(5L, "제목5", "작성자5", 0, LocalDateTime.now()),
                new PostListItem(4L, "제목4", "작성자4", 0, LocalDateTime.now()),
                new PostListItem(3L, "제목3", "작성자3", 0, LocalDateTime.now()),
                new PostListItem(2L, "제목2", "작성자2", 0, LocalDateTime.now()),
                new PostListItem(1L, "제목1", "작성자1", 0, LocalDateTime.now())
        );
        PageImpl<PostListItem> postPage = new PageImpl<>(content);

        given(postRepository.findPosts(any(Pageable.class))).willReturn(postPage);

        postService.postList(1);

        then(postRepository).should().findPosts(any(Pageable.class));
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void postModify() {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        given(postRepository.findPostJoinFetch(anyLong())).willReturn(Optional.of(post));

        postService.postModify(1L, postModifyRequest, "yoon1234");

        then(postRepository).should().findPostJoinFetch(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postModify_notFoundPost() {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");

        willThrow(new NotFoundPostException()).given(postRepository).findPostJoinFetch(anyLong());

        assertThatThrownBy(() -> postService.postModify(1L, postModifyRequest, "yoon1234"))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findPostJoinFetch(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 시 작성자가 아닌데 수정을 시도할 경우 예외가 발생한다")
    void postModify_notPostOwner() {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        given(postRepository.findPostJoinFetch(anyLong())).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.postModify(1L, postModifyRequest, "unknown"))
                .isInstanceOf(PostModifyAccessDeniedException.class);

        then(postRepository).should().findPostJoinFetch(anyLong());
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void postDelete() {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        given(postRepository.findPostJoinFetch(anyLong())).willReturn(Optional.of(post));
        willDoNothing().given(postRepository).delete(any(Post.class));

        postService.postDelete(1L, "yoon1234");

        then(postRepository).should().findPostJoinFetch(anyLong());
        then(postRepository).should().delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDelete_notFoundPost() {
        willThrow(new NotFoundPostException()).given(postRepository).findPostJoinFetch(anyLong());

        assertThatThrownBy(() -> postService.postDelete(1L, "yoon1234"))
                .isInstanceOf(NotFoundPostException.class);

        then(postRepository).should().findPostJoinFetch(anyLong());
        then(postRepository).should(never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 시 작성자가 아닌데 삭제를 시도할 경우 예외가 발생한다")
    void postDelete_notPostOwner() {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        given(postRepository.findPostJoinFetch(anyLong())).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.postDelete(1L, "unknown"))
                .isInstanceOf(PostDeleteAccessDeniedException.class);

        then(postRepository).should().findPostJoinFetch(anyLong());
        then(postRepository).should(never()).delete(any(Post.class));
    }

}