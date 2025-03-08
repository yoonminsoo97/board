package com.backend.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

import java.util.List;

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

    public PostListResponse(Page<PostItem> postPage) {
        this.posts = postPage.getContent();
        this.page = postPage.getNumber() + 1;
        this.totalPages = postPage.getTotalPages();
        this.totalPosts = postPage.getTotalElements();
        this.first = postPage.isFirst();
        this.last = postPage.isLast();
        this.prev = postPage.hasPrevious();
        this.next = postPage.hasNext();
    }

}
