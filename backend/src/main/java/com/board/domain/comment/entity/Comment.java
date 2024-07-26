package com.board.domain.comment.entity;

import com.board.domain.member.entity.Member;
import com.board.domain.post.entity.Post;
import com.board.global.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isDelete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_id")
    private Comment reference;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reference", cascade = CascadeType.PERSIST)
    private List<Comment> replies = new ArrayList<>();

    @Builder
    public Comment(String content, String writer, Member member, Post post, Comment reference) {
        this.writer = writer;
        this.content = content;
        this.member = member;
        this.reference = reference;
        this.isDelete = false;
        this.post = post;
    }

    public void addReply(Comment reply) {
        reply.getReplies().add(this);
        this.replies.add(reply);
    }

    public void modify(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.isDelete = true;
    }

    public void deleteReply(Comment reply) {
        reply.getReplies().remove(this);
        replies.remove(reply);
    }

    public boolean isReply() {
        return Objects.nonNull(reference);
    }

    public boolean isOwner(Long memberId) {
        return member.getId().equals(memberId);
    }

    public boolean hasReplies() {
        return !replies.isEmpty();
    }

}
