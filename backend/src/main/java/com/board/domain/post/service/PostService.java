package com.board.domain.post.service;

import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.domain.post.exception.PostDeleteAccessDeniedException;
import com.board.domain.post.exception.PostModifyAccessDeniedException;
import com.board.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

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
    public PostDetailResponse postDetail(Long postNumber) {
        Post post = postRepository.findById(postNumber)
                .orElseThrow(NotFoundPostException::new);
        return new PostDetailResponse(post);
    }

    @Transactional
    public void postModify(Long postNumber, PostModifyRequest postModifyRequest, String loginUsername) {
        Post post = postRepository.findPostJoinFetch(postNumber)
                .orElseThrow(NotFoundPostException::new);
        if (!post.isOwner(loginUsername)) {
            throw new PostModifyAccessDeniedException();
        }
        post.modify(postModifyRequest.getTitle(), postModifyRequest.getContent());
    }

    @Transactional
    public void postDelete(Long postNumber, String loginUsername) {
        Post post = postRepository.findPostJoinFetch(postNumber)
                .orElseThrow(NotFoundPostException::new);
        if (!post.isOwner(loginUsername)) {
            throw new PostDeleteAccessDeniedException();
        }
        postRepository.delete(post);
    }

}
