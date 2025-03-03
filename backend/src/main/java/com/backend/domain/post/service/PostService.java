package com.backend.domain.post.service;

import com.backend.domain.member.entity.Member;
import com.backend.domain.member.exception.NotFoundMemberException;
import com.backend.domain.member.repository.MemberRepository;
import com.backend.domain.post.dto.PostDetailResponse;
import com.backend.domain.post.dto.PostListResponse;
import com.backend.domain.post.dto.PostModifyRequest;
import com.backend.domain.post.dto.PostWriteRequest;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.exception.NotFoundPostException;
import com.backend.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public void postWrite(PostWriteRequest postWriteRequest, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(NotFoundMemberException::new);
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
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundPostException::new);
        return new PostDetailResponse(post);
    }

    @Transactional(readOnly = true)
    public PostListResponse postListResponse(int page) {
        page = page <= 0 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "id");
        Page<Post> postPage = postRepository.findAll(pageable);
        return new PostListResponse(postPage);
    }

    @Transactional
    public void postModify(Long postId, PostModifyRequest postModifyRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundPostException::new);
        post.modify(postModifyRequest.getTitle(), postModifyRequest.getContent());
    }

    @Transactional
    public void postDelete(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundPostException::new);
        postRepository.delete(post);
    }

}
