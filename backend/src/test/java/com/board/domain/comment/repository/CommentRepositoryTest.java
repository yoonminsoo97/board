package com.board.domain.comment.repository;

import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.comment.entity.Comment;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.member.dto.MemberCommentListResponse;
import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.post.entity.Post;
import com.board.domain.post.repository.PostRepository;
import com.board.support.RepositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentRepositoryTest extends RepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        memberRepository.save(member);
        post = Post.builder()
                .title("title")
                .writer(member.getNickname())
                .content("content")
                .member(member)
                .build();
        postRepository.save(post);
    }

    @Nested
    @DisplayName("댓글 저장")
    class CommentSaveTest {

        @Test
        @DisplayName("댓글을 저장한다")
        void save() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();

            Comment saveComment = commentRepository.save(comment);

            assertThat(saveComment.getId()).isNotNull();
        }

        @Test
        @DisplayName("작성자가 null이면 에외가 발생한다")
        void saveNullWriter() {
            Comment comment = Comment.builder()
                    .writer(null)
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();

            assertThatThrownBy(() -> commentRepository.save(comment))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("내용이 null이면 예외가 발생한다")
        void saveNullContent() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content(null)
                    .member(member)
                    .post(post)
                    .build();

            assertThatThrownBy(() -> commentRepository.save(comment))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("회원이 null이면 예외가 발생한다")
        void saveNullMember() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(null)
                    .post(post)
                    .build();

            assertThatThrownBy(() -> commentRepository.save(comment))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("게시글이 null이면 예외가 발생한다")
        void saveNullPost() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(null)
                    .build();

            assertThatThrownBy(() -> commentRepository.save(comment))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("대댓글을 저장한다")
        void saveReply() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            commentRepository.save(comment);

            Comment reply = Comment.builder()
                    .writer(member.getNickname())
                    .content("reply")
                    .member(member)
                    .post(post)
                    .reference(comment)
                    .build();

            Comment saveReply = commentRepository.save(reply);

            assertThat(saveReply.getId()).isNotNull();
            assertThat(reply.getReference()).isEqualTo(comment);
        }

    }


    @Nested
    @DisplayName("댓글 조회")
    class CommentFindTest {

        @Test
        @DisplayName("댓글 기본키와 게시글 기본키로 조회한다")
        void findByPostIdAndCommentId() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            commentRepository.save(comment);

            Comment findComment = commentRepository.findCommentByPostIdAndCommentId(post.getId(), comment.getId())
                    .orElseThrow(NotFoundCommentException::new);

            assertThat(findComment.getWriter()).isEqualTo("yoonkun");
            assertThat(findComment.getContent()).isEqualTo("comment");
            assertThat(findComment.isDelete()).isFalse();
        }

        @Test
        @DisplayName("댓글이 존재하지 않으면 예외가 발생한다")
        void findByPostIdAndCommentIdNotFoundComment() {
            assertThatThrownBy(() -> commentRepository.findCommentByPostIdAndCommentId(post.getId(), 1L)
                    .orElseThrow(NotFoundCommentException::new))
                    .isInstanceOf(NotFoundCommentException.class);
        }

    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class CommentListFindTest {

        @Test
        @DisplayName("특정 게시글에 속한 댓글 목록을 조회한다")
        void findCommentListByPostId() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            Comment reply = Comment.builder()
                    .writer(member.getNickname())
                    .content("reply")
                    .member(member)
                    .post(post)
                    .reference(comment)
                    .build();
            commentRepository.save(comment);
            commentRepository.save(reply);

            List<CommentListResponse.CommentItem> commentList = commentRepository.findCommentListByPostId(post.getId());

            assertThat(commentList.isEmpty()).isFalse();
            assertThat(commentList.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("특정 게시글에 속한 대댓글 목록을 조회한다")
        void findReplyListByPostId() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            Comment reply = Comment.builder()
                    .writer(member.getNickname())
                    .content("reply")
                    .member(member)
                    .post(post)
                    .reference(comment)
                    .build();
            commentRepository.save(comment);
            commentRepository.save(reply);

            List<CommentListResponse.CommentItem.ReplyItem> replyList = commentRepository.findReplyListByPostId(post.getId());

            assertThat(replyList.isEmpty()).isFalse();
            assertThat(replyList.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("회원이 작성한 댓글 목록을 조회한다")
        void findMemberCommentList() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            Comment reply = Comment.builder()
                    .writer(member.getNickname())
                    .content("reply")
                    .member(member)
                    .post(post)
                    .reference(comment)
                    .build();
            commentRepository.save(comment);
            commentRepository.save(reply);

            MemberCommentListResponse memberCommentList = commentRepository.findMemberCommentList(PageRequest.of(0, 10), member.getId());

            assertThat(memberCommentList.getPage()).isEqualTo(1);
            assertThat(memberCommentList.getTotalElements()).isEqualTo(2);
            assertThat(memberCommentList.getTotalPages()).isEqualTo(1);
            assertThat(memberCommentList.isFirst()).isTrue();
            assertThat(memberCommentList.isLast()).isTrue();
            assertThat(memberCommentList.isPrev()).isFalse();
            assertThat(memberCommentList.isNext()).isFalse();
        }

    }

    @Nested
    @DisplayName("댓글 삭제")
    class CommentDeleteTest {

        @Test
        @DisplayName("댓글을 논리 삭제한다")
        void softDelete() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            commentRepository.save(comment);

            Comment findComment = commentRepository.findCommentByPostIdAndCommentId(post.getId(), comment.getId())
                    .orElseThrow(NotFoundCommentException::new);

            findComment.softDelete();

            assertThat(findComment.isDelete()).isTrue();
        }

        @Test
        @DisplayName("댓글을 물리 삭제한다")
        void hardDelete() {
            Comment comment = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            commentRepository.save(comment);

            Comment findComment = commentRepository.findCommentByPostIdAndCommentId(post.getId(), comment.getId())
                    .orElseThrow(NotFoundCommentException::new);

            commentRepository.delete(findComment);

            assertThat(commentRepository.findCommentByPostIdAndCommentId(post.getId(), comment.getId())).isEmpty();
        }

        @Test
        @DisplayName("특정 게시글에 속한 댓글들을 삭제한다")
        void deleteByPostId() {
            Comment commentA = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            Comment commentB = Comment.builder()
                    .writer(member.getNickname())
                    .content("comment")
                    .member(member)
                    .post(post)
                    .build();
            commentRepository.save(commentA);
            commentRepository.save(commentB);

            commentRepository.deleteByPostId(post.getId());

            assertThat(commentRepository.findCommentListByPostId(post.getId()).size()).isEqualTo(0);
        }

    }

}