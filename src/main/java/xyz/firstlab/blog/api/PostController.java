package xyz.firstlab.blog.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import xyz.firstlab.blog.dto.MessageResponse;
import xyz.firstlab.blog.dto.PostCreateRequest;
import xyz.firstlab.blog.dto.PostInfoResponse;
import xyz.firstlab.blog.dto.PostUpdateRequest;
import xyz.firstlab.blog.security.JpaUserDetails;
import xyz.firstlab.blog.service.PostService;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public PostInfoResponse createPost(@RequestBody PostCreateRequest postCreateRequest) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails principal = (JpaUserDetails) context.getAuthentication().getPrincipal();
        String username = principal.getUser().getUsername();

        return postService.createPost(username, postCreateRequest);
    }

    @GetMapping("/{postId}")
    public PostInfoResponse readPost(@PathVariable("postId") Long postId) {
        return postService.readPost(postId);
    }

    @PutMapping("/{postId}")
    public PostInfoResponse updatePost(
            @PathVariable("postId") Long postId, @RequestBody PostUpdateRequest postUpdateRequest) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails principal = (JpaUserDetails) context.getAuthentication().getPrincipal();
        Long userId = principal.getUser().getId();

        return postService.updatePost(postId, userId, postUpdateRequest);
    }

    @DeleteMapping("/{postId}")
    public MessageResponse deletePost(@PathVariable("postId") Long postId) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails principal = (JpaUserDetails) context.getAuthentication().getPrincipal();
        Long userId = principal.getUser().getId();

        postService.deletePost(postId, userId);
        return new MessageResponse("Post is successfully deleted.");
    }

}
