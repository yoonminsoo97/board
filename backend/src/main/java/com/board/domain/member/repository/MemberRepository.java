package com.board.domain.member.repository;

import com.board.domain.member.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsMemberByNickname(String nickname);
    boolean existsMemberByUsername(String username);
    Optional<Member> findMemberByUsername(String username);

}
