package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.post.Post;
import xyz.firstlab.blog.entity.user.User;

public record PostCreateRequest(
        String title,
        String content
) {
    public Post toPost(User user) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(user)
                .build();
    }
}
