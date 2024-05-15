package com.board.domain.reply.repository;

import com.board.domain.reply.entity.Reply;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}
