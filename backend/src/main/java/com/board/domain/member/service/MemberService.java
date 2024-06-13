package com.board.domain.member.service;

import com.board.domain.member.dto.MemberNicknameRequest;
import com.board.domain.member.dto.MemberPasswordRequest;
import com.board.domain.member.dto.MemberProfileResponse;
import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int POST_PER_PAGE = 10;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void memberNicknameExists(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }
    }

    @Transactional
    public void memberUsernameExists(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException();
        }
    }

    @Transactional
    public void memberSignup(MemberSignupRequest memberSignupRequest) {
        memberNicknameExists(memberSignupRequest.getNickname());
        memberUsernameExists(memberSignupRequest.getUsername());
        if (!memberSignupRequest.getPassword().equals(memberSignupRequest.getPasswordConfirm())) {
            throw new PasswordMismatchException();
        }
        String enocoded = passwordEncoder.encode(memberSignupRequest.getPassword());
        Member member = Member.builder()
                .nickname(memberSignupRequest.getNickname())
                .username(memberSignupRequest.getUsername())
                .password(enocoded)
                .build();
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse memberProfile(Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        return MemberProfileResponse.of(member);
    }

    @Transactional(readOnly = true)
    public PostListResponse memberPostList(int page, Long memberId) {
        return postRepository.findPostMemberList(PageRequest.of(page, POST_PER_PAGE), memberId);
    }

    @Transactional
    public void memberNicknameChange(MemberNicknameRequest memberNicknameRequest, Long memberId) {
        memberNicknameExists(memberNicknameRequest.getNickname());
        Member member = memberRepository.findByMemberId(memberId);
        member.changeNickname(memberNicknameRequest.getNickname());
    }

    @Transactional
    public void memberPasswordChange(MemberPasswordRequest memberPasswordRequest, Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        if (notMatchCurPassword(memberPasswordRequest.getCurPassword(), member.getPassword())) {
            throw new PasswordMismatchException();
        }
        if (notMatchNewPassword(memberPasswordRequest.getNewPassword(), memberPasswordRequest.getNewPasswordConfirm())) {
            throw new PasswordMismatchException();
        }
        member.changePassword(memberPasswordRequest.getNewPassword());
    }

    private boolean notMatchCurPassword(String curRawPassword, String curEncodedPassword) {
        return !passwordEncoder.matches(curRawPassword, curEncodedPassword);
    }

    private boolean notMatchNewPassword(String newPassword, String newPasswordConfirm) {
        return !newPassword.equals(newPasswordConfirm);
    }

}
