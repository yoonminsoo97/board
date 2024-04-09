package com.board.domain.token.repository;

import com.board.domain.token.entity.Token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("SELECT t FROM Token AS t WHERE t.member.username = :username")
    Optional<Token> findByMemberUsername(@Param("username") String username);

    @Query("SELECT t FROM Token AS t JOIN FETCH t.member AS m WHERE m.username = :username")
    Optional<Token> findTokenJoinFetchMember(@Param("username") String username);

}
