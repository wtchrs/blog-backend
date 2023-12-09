package xyz.firstlab.blog.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import xyz.firstlab.blog.dto.MessageResponse;
import xyz.firstlab.blog.dto.ArticleCreateRequest;
import xyz.firstlab.blog.dto.ArticleInfoResponse;
import xyz.firstlab.blog.dto.ArticleUpdateRequest;
import xyz.firstlab.blog.security.JpaUserDetails;
import xyz.firstlab.blog.service.ArticleService;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public ArticleInfoResponse createPost(@RequestBody ArticleCreateRequest articleCreateRequest) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails principal = (JpaUserDetails) context.getAuthentication().getPrincipal();
        String username = principal.getUser().getUsername();

        return articleService.postArticle(username, articleCreateRequest);
    }

    @GetMapping("/{articleId}")
    public ArticleInfoResponse readPost(@PathVariable("articleId") Long articleId) {
        return articleService.readArticle(articleId);
    }

    @PutMapping("/{articleId}")
    public ArticleInfoResponse updatePost(
            @PathVariable("articleId") Long articleId, @RequestBody ArticleUpdateRequest articleUpdateRequest) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails principal = (JpaUserDetails) context.getAuthentication().getPrincipal();
        Long userId = principal.getUser().getId();

        return articleService.updateArticle(articleId, userId, articleUpdateRequest);
    }

    @DeleteMapping("/{articleId}")
    public MessageResponse deletePost(@PathVariable("articleId") Long articleId) {
        SecurityContext context = SecurityContextHolder.getContext();
        JpaUserDetails principal = (JpaUserDetails) context.getAuthentication().getPrincipal();
        Long userId = principal.getUser().getId();

        articleService.deleteArticle(articleId, userId);
        return new MessageResponse("Post is successfully deleted.");
    }

}
