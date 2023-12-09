package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.article.Article;

import java.time.LocalDateTime;

public record ArticleInfoResponse(
        Long articleId,
        String username,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        int views
) {
    public static ArticleInfoResponse from(Article article) {
        return new ArticleInfoResponse(
                article.getId(), article.getAuthor().getUsername(), article.getTitle(), article.getContent(),
                article.getCreatedAt(), article.getModifiedAt(), article.getViews());
    }
}
