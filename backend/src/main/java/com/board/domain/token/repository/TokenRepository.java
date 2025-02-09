package com.board.domain.token.repository;

import com.board.domain.token.entity.Token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByMemberUsername(String username);

}
