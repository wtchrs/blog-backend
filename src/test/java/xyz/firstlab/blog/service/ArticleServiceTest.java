package xyz.firstlab.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.ArticleCreateRequest;
import xyz.firstlab.blog.dto.ArticleInfoResponse;
import xyz.firstlab.blog.dto.ArticleUpdateRequest;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.article.ArticleRepository;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    ArticleRepository articleRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ArticleService articleService;

    @Test
    void postArticle_Success() {
        User testUser = createTestUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ArticleCreateRequest articleCreateRequest = new ArticleCreateRequest("test title", "This is content.");
        ArticleInfoResponse result = articleService.postArticle(1L, articleCreateRequest);

        assertThat(result.title()).isEqualTo(articleCreateRequest.title());
        assertThat(result.content()).isEqualTo(articleCreateRequest.content());
        assertThat(result.views()).isEqualTo(0);
        assertThat(result.username()).isEqualTo("testUser");

        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    void postArticle_UserNotExists() {
        ArticleCreateRequest articleCreateRequest = new ArticleCreateRequest("test title", "This is content.");

        assertThatThrownBy(() -> articleService.postArticle(1L, articleCreateRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void readArticle_Successful() {
        User testUser = createTestUser();
        Article mockArticle = Mockito.spy(createTestArticle(testUser));
        when(mockArticle.getId()).thenReturn(10L);

        when(articleRepository.findById(10L)).thenReturn(Optional.of(mockArticle));

        ArticleInfoResponse result = articleService.readArticle(10L);

        assertThat(result.articleId()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo(mockArticle.getTitle());
        assertThat(result.content()).isEqualTo(mockArticle.getContent());
        assertThat(result.username()).isEqualTo(mockArticle.getAuthor().getUsername());

        verify(mockArticle, times(1)).increaseViews();
    }

    @Test
    void readArticle_ArticleNotExists() {
        when(articleRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.readArticle(10L))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void updateArticle_Successful() {
        User testUser = Mockito.spy(createTestUser());
        when(testUser.getId()).thenReturn(1L);
        Article testArticle = createTestArticle(testUser);

        when(articleRepository.findById(10L)).thenReturn(Optional.of(testArticle));

        ArticleUpdateRequest articleUpdateRequest = new ArticleUpdateRequest("new title", "new content");
        ArticleInfoResponse result = articleService.updateArticle(10L, testUser.getId(), articleUpdateRequest);

        assertThat(testArticle.getTitle()).isEqualTo(articleUpdateRequest.title());
        assertThat(testArticle.getContent()).isEqualTo(articleUpdateRequest.content());
        assertThat(testArticle.getAuthor()).isSameAs(testUser);

        assertThat(result.title()).isEqualTo(articleUpdateRequest.title());
        assertThat(result.content()).isEqualTo(articleUpdateRequest.content());
        assertThat(result.username()).isEqualTo(testArticle.getAuthor().getUsername());
    }

    @Test
    void updateArticle_ArticleNotExists() {
        when(articleRepository.findById(any())).thenReturn(Optional.empty());

        ArticleUpdateRequest articleUpdateRequest = new ArticleUpdateRequest("new title", "new content");

        assertThatThrownBy(() -> articleService.updateArticle(10L, 1L, articleUpdateRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteArticle_Successful() {
        User testUser = Mockito.spy(createTestUser());
        when(testUser.getId()).thenReturn(1L);
        Article testArticle = createTestArticle(testUser);

        when(articleRepository.findById(10L)).thenReturn(Optional.of(testArticle));

        articleService.deleteArticle(10L, testUser.getId());

        assertThat(testArticle.isDeleted()).isTrue();
    }

    @Test
    void deleteArticle_ArticleNotExists() {
        when(articleRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.deleteArticle(10L, 1L))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    private static Article createTestArticle(User user) {
        return Article.builder()
                .title("test title")
                .content("This is content.")
                .author(user)
                .build();
    }

    private static User createTestUser() {
        return User.builder()
                .username("testUser")
                .password("password")
                .name("name")
                .blogName("blogName")
                .greeting("Hello!")
                .build();
    }

}