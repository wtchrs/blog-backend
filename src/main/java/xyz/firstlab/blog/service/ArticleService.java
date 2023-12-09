package xyz.firstlab.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.ArticleCreateRequest;
import xyz.firstlab.blog.dto.ArticleInfoResponse;
import xyz.firstlab.blog.dto.ArticleUpdateRequest;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.article.ArticleRepository;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final UserRepository userRepository;

    private final ArticleRepository articleRepository;

    @Transactional
    public ArticleInfoResponse postArticle(String username, ArticleCreateRequest articleCreateRequest) {
        // TODO: Instead of reading the entire entity,
        //  change it to only check whether it exists and whether it has been deleted.

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User '" + username + "' is not exists.");
        }

        User user = optionalUser.get();
        if (user.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User '" + username + "' is not exists.");
        }

        Article article = articleCreateRequest.toArticle(user);
        articleRepository.save(article);
        return ArticleInfoResponse.from(article);
    }

    @Transactional(readOnly = true)
    public ArticleInfoResponse readArticle(Long postId) {
        Article article = getArticle(postId);
        article.increaseViews();
        return ArticleInfoResponse.from(article);
    }

    private Article getArticle(Long articleId) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article id '" + articleId + "' is not exists.");
        }

        Article article = optionalArticle.get();
        if (article.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article id '" + articleId + "' is not exists.");
        }

        return article;
    }

    @Transactional
    public ArticleInfoResponse updateArticle(Long postId, Long userId, ArticleUpdateRequest articleUpdateRequest) {
        Article article = getArticle(postId);

        if (!userId.equals(article.getAuthor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author of this article can update it.");
        }

        article.update(articleUpdateRequest.title(), articleUpdateRequest.content());
        return ArticleInfoResponse.from(article);
    }

    @Transactional
    public void deleteArticle(Long postId, Long userId) {
        Article article = getArticle(postId);

        if (!userId.equals(article.getAuthor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author of this article can delete it.");
        }

        article.delete();
    }

}
