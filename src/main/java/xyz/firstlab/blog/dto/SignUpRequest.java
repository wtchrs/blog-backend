package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.user.User;

public record SignUpRequest(
        String username,
        String password,
        String passwordConfirmation,
        String name,
        String blogName,
        String greeting
) {
    public User toUser() {
        return User.builder()
                .username(username)
                .password(password)
                .name(name)
                .blogName(blogName)
                .greeting(greeting)
                .build();
    }
}
