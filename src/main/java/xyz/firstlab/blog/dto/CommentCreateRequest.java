package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.comment.Comment;
import xyz.firstlab.blog.entity.user.User;

public record CommentCreateRequest (
        Long articleId,
        String content
) {
    public Comment toComment(User user, Article article) {
        return Comment.builder()
                .user(user)
                .article(article)
                .content(content)
                .build();
    }
}
