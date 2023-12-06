package xyz.firstlab.blog.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.*;
import xyz.firstlab.blog.security.JpaUserDetails;
import xyz.firstlab.blog.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Transactional
    @PostMapping
    public UserInfoResponse createUser(@RequestBody SignUpRequest signUp) {
        return userService.signUp(signUp);
    }

    @GetMapping("/{username}")
    public UserInfoResponse userInfo(@PathVariable("username") String username) {
        return userService.getUserInfo(username);
    }

    @PutMapping("/{username}")
    public UserInfoResponse update(
            @PathVariable("username") String username, @RequestBody UserUpdateRequest userUpdate) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails userDetails = (JpaUserDetails) context.getAuthentication().getPrincipal();

        if (!userDetails.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Do not have the user update privilege.");
        }

        return userService.update(username, userUpdate);
    }

    @DeleteMapping("/{username}")
    public MessageResponse delete(@PathVariable("username") String username) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails userDetails = (JpaUserDetails) context.getAuthentication().getPrincipal();

        if (!userDetails.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Do not have the user delete privilege.");
        }

        userService.delete(username);
        return new MessageResponse("User is successfully deleted.");
    }

}
