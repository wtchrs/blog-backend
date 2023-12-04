package xyz.firstlab.blog.dto;

public record ErrorResponse(
        String error,
        String message
) {
}
