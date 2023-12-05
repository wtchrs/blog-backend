package xyz.firstlab.blog.dto;

public record UserUpdateRequest(
        String name,
        String blogName,
        String greeting
) {
}
