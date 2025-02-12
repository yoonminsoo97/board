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
import com.board.domain.post.repository.PostRepository;

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

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void postWrite(String username, PostWriteRequest postWriteRequest) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(NotFoundMemberException::new);
        Post post = Post.builder()
                .title(postWriteRequest.getTitle())
                .writer(member.getNickname())
                .member(member)
                .content(postWriteRequest.getContent())
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
    public PostListResponse postList(int page) {
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
