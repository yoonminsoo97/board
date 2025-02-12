package com.board.domain.post.dto;

import com.board.domain.post.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {

    private List<PostItem> posts;
    private int page;
    private int totalPages;
    private long totalPosts;
    private boolean first;
    private boolean last;
    private boolean prev;
    private boolean next;

    public PostListResponse(Page<Post> postPage) {
        this.posts = postPage.getContent().stream()
                .map(PostItem::new)
                .collect(Collectors.toList());
        this.page = postPage.getNumber() + 1;
        this.totalPages = postPage.getTotalPages();
        this.totalPosts = postPage.getTotalElements();
        this.first = postPage.isFirst();
        this.last = postPage.isLast();
        this.prev = postPage.hasPrevious();
        this.next = postPage.hasNext();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostItem {

        private Long postId;
        private String title;
        private String writer;
        private LocalDateTime createdAt;

        public PostItem(Post post) {
            this.postId = post.getId();
            this.title = post.getTitle();
            this.writer = post.getWriter();
            this.createdAt = post.getCreatedAt();
        }

    }

}
