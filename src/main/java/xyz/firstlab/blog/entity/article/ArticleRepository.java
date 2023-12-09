package xyz.firstlab.blog.entity.article;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.firstlab.blog.entity.user.User;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findPostsByAuthor(User author);

}
