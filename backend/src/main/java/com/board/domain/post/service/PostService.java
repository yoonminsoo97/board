package com.board.domain.post.service;

import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.domain.post.exception.PostDeleteAccessDeniedException;
import com.board.domain.post.exception.PostModifyAccessDeniedException;
import com.board.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int POST_PER_PAGE = 10;

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void postWrite(PostWriteRequest postWriteRequest, String username) {
        Member member = memberRepository.findMemberByUsername(username)
                .orElseThrow(NotFoundMemberException::new);
        Post post = Post.builder()
                .title(postWriteRequest.getTitle())
                .content(postWriteRequest.getContent())
                .member(member)
                .build();
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse postDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundPostException::new);
        return PostDetailResponse.of(post);
    }

    @Transactional(readOnly = true)
    public PostListResponse postList(int page) {
        return postRepository.findPosts(PageRequest.of(page, POST_PER_PAGE));
    }

    @Transactional(readOnly = true)
    public PostListResponse postListSearch(int page, String type, String keyword) {
        return postRepository.findSearchPosts(PageRequest.of(page, POST_PER_PAGE), type, keyword);
    }

    @Transactional
    public void postModify(Long postId, PostModifyRequest postModifyRequest, String username) {
        Post post = postRepository.findPostJoinFetch(postId)
                .orElseThrow(NotFoundPostException::new);
        if (!post.isOwner(username)) {
            throw new PostModifyAccessDeniedException();
        }
        post.modify(postModifyRequest.getTitle(), postModifyRequest.getContent());
    }

    @Transactional
    public void postDelete(Long postId, String loginUsername) {
        Post post = postRepository.findPostJoinFetch(postId)
                .orElseThrow(NotFoundPostException::new);
        if (!post.isOwner(loginUsername)) {
            throw new PostDeleteAccessDeniedException();
        }
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostListResponse postListFromMember(int page, String username) {
        return null;
    }

}
