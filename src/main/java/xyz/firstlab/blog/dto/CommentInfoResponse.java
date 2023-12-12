package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.comment.Comment;

import java.time.LocalDateTime;

public record CommentInfoResponse(
        Long commentId,
        Long articleId,
        String username,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static CommentInfoResponse from(Comment comment) {
        return new CommentInfoResponse(
                comment.getId(), comment.getArticle().getId(), comment.getUser().getUsername(), comment.getContent(),
                comment.getCreatedAt(), comment.getModifiedAt());
    }
}
