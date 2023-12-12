package xyz.firstlab.blog.entity.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.user.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // TODO: add cursor-based pagination queries.

    Slice<Comment> findCommentsByArticle(Article article, Pageable pageable);

    Slice<Comment> findCommentsByArticleId(Long articleId, Pageable pageable);

    Slice<Comment> findCommentsByUser(User user, Pageable pageable);

    Slice<Comment> findCommentsByUserId(Long userId, Pageable pageable);

}
