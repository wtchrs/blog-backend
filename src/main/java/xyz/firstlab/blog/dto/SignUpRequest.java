package xyz.firstlab.blog.dto;

public record SignUpRequest(
        String username,
        String password,
        String passwordConfirm
) {
}
