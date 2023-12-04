package xyz.firstlab.blog.dto;

import java.time.LocalDateTime;

public record SignUpResponse(
        String username,
        LocalDateTime signUpDate
) {
}
