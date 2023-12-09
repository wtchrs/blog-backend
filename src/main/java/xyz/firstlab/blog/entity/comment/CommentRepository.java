package xyz.firstlab.blog.entity.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.user.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findCommentsByArticle(Article article);

    List<Comment> findCommentsByUser(User user);

}
