package xyz.firstlab.blog.entity.post;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.firstlab.blog.entity.user.User;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findPostsByAuthor(User author);

}
