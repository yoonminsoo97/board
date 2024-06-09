package com.board.domain.token.repository;

import com.board.domain.token.entity.Token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByMemberId(Long memberId);

    @Query("SELECT t FROM Token AS t JOIN FETCH t.member WHERE t.refreshToken = :refreshToken")
    Optional<Token> findByRefreshTokenJoinFetchMember(@Param("refreshToken") String refreshToken);

}
