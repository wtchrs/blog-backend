package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.user.User;

public record ArticleCreateRequest(
        String title,
        String content
) {
    public Article toArticle(User user) {
        return Article.builder()
                .title(title)
                .content(content)
                .author(user)
                .build();
    }
}
