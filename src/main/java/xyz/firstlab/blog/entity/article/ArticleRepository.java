package xyz.firstlab.blog.entity.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.firstlab.blog.entity.user.User;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findPostsByAuthor(User author, Pageable pageable);

    Page<Article> findPostsByAuthorId(Long authorId, Pageable pageable);

}
