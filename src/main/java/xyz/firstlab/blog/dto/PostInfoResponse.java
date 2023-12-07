package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.post.Post;

import java.time.LocalDateTime;

public record PostInfoResponse(
        Long postId,
        String username,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        int views
) {
    public static PostInfoResponse from(Post post) {
        return new PostInfoResponse(
                post.getId(), post.getAuthor().getUsername(), post.getTitle(), post.getContent(), post.getCreatedAt(),
                post.getModifiedAt(), post.getViews());
    }
}
