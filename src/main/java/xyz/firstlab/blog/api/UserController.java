package xyz.firstlab.blog.api;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.firstlab.blog.dto.*;
import xyz.firstlab.blog.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Transactional
    @PostMapping
    public UserInfoResponse signUp(@RequestBody SignUpRequest signUp) {
        return userService.signUp(signUp);
    }

}
