package com.board.domain.member.service;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 로직을 수행한다.
     * <p>
     * 1. 닉네임 중복 검증 <br>
     * 2. 아이디 중복 검증 <br>
     * 3. 비밀번호 암호화 <br>
     * 4. 회원 저장
     *
     * @param memberSignupRequest 회원가입 요청 데이터(닉네임, 아이디, 비밀번호)
     * @throws DuplicateNicknameException 닉네임이 이미 존재하는 경우 발생
     * @throws DuplicateUsernameException 아이디가 이미 존재하는 경우 발생
     */
    @Transactional
    public void memberSignup(MemberSignupRequest memberSignupRequest) {
        if (memberRepository.existsByNickname(memberSignupRequest.getNickname())) {
            throw new DuplicateNicknameException();
        }
        if (memberRepository.existsByUsername(memberSignupRequest.getUsername())) {
            throw new DuplicateUsernameException();
        }
        String encodedPassword = passwordEncoder.encode(memberSignupRequest.getPassword());
        Member member = Member.builder()
                .nickname(memberSignupRequest.getNickname())
                .username(memberSignupRequest.getUsername())
                .password(encodedPassword)
                .build();
        memberRepository.save(member);
    }

}
