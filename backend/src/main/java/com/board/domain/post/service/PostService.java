package com.board.domain.post.service;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
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

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public void postWrite(PostWriteRequest postWriteRequest, Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        Post post = Post.builder()
                .title(postWriteRequest.getTitle())
                .writer(member.getNickname())
                .content(postWriteRequest.getContent())
                .member(member)
                .build();
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse postDetail(Long postId) {
        Post post = postRepository.findByPostId(postId);
        return PostDetailResponse.of(post);
    }

    @Transactional(readOnly = true)
    public PostListResponse postList(int page) {
        return postRepository.findPostList(PageRequest.of(page, POST_PER_PAGE));
    }

    @Transactional(readOnly = true)
    public PostListResponse postSearchList(int page, String type, String keyword) {
        return postRepository.findPostSearchList(PageRequest.of(page, POST_PER_PAGE), type, keyword);
    }

    @Transactional
    public void postModify(Long postId, PostModifyRequest postModifyRequest, Long memberId) {
        Post post = postRepository.findByPostId(postId);
        if (!post.isOwner(memberId)) {
            throw new PostModifyAccessDeniedException();
        }
        post.modify(postModifyRequest.getTitle(), postModifyRequest.getContent());
    }

    @Transactional
    public void postDelete(Long postId, Long memberId) {
        Post post = postRepository.findByPostId(postId);
        if (!post.isOwner(memberId)) {
            throw new PostDeleteAccessDeniedException();
        }
        postRepository.delete(post);
    }

}
