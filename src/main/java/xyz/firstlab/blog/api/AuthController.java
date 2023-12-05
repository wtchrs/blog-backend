package xyz.firstlab.blog.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.firstlab.blog.dto.*;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public SignInResponse signIn(HttpServletRequest request, @RequestBody SignInRequest signIn) {
        User user = authService.signIn(request, signIn);
        return new SignInResponse(user.getName(), user.getBlogName());
    }

    @PostMapping("/sign-out")
    public MessageResponse signOut(HttpServletRequest request, HttpServletResponse response) {
        authService.signOut(request, response);
        return new MessageResponse("Successfully signed out.");
    }

}
