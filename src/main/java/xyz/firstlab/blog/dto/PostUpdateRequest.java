package xyz.firstlab.blog.dto;

public record PostUpdateRequest(
        String title,
        String content
) {
}
