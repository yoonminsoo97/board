package com.backend.domain.auth.repository;

import com.backend.domain.auth.entity.Token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByMemberUsername(String username);

}
