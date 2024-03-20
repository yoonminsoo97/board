package com.board.domain.post.dto;

import com.board.domain.post.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class PostListResponse {

    private final List<PostListItem> posts;
    private final int pageNumber;
    private final int totalPages;
    private final long totalElements;
    private final boolean prev;
    private final boolean next;
    private final boolean first;
    private final boolean last;

    public PostListResponse(Page<Post> postPage) {
        this.posts = postPage.getContent().stream()
                .map(PostListItem::new)
                .collect(Collectors.toList());
        this.pageNumber = postPage.getNumber() + 1;
        this.totalPages = postPage.getTotalPages();
        this.totalElements = postPage.getTotalElements();
        this.prev = postPage.hasPrevious();
        this.next = postPage.hasNext();
        this.first = postPage.isFirst();
        this.last = postPage.isLast();
    }

}
