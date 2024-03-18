package com.board.domain.token.repository;

import com.board.domain.token.entity.Token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
}
