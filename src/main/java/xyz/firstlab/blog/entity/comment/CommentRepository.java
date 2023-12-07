package xyz.firstlab.blog.entity.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.firstlab.blog.entity.post.Post;
import xyz.firstlab.blog.entity.user.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findCommentsByPost(Post post);

    List<Comment> findCommentsByUser(User user);

}
