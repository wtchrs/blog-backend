package xyz.firstlab.blog.dto;

import xyz.firstlab.blog.entity.user.User;

import java.time.LocalDateTime;

public record UserInfoResponse(
        String username,
        String name,
        String blogName,
        String greeting,
        LocalDateTime signUpDate
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getUsername(), user.getName(), user.getBlogName(), user.getGreeting(), user.getCreatedAt());
    }
}
