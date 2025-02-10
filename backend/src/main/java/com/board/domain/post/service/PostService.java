package com.board.domain.post.service;

import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.entity.Post;
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

}
