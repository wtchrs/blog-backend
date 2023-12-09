package xyz.firstlab.blog.dto;

public record ArticleUpdateRequest(
        String title,
        String content
) {
}
