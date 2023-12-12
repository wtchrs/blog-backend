package xyz.firstlab.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.CommentCreateRequest;
import xyz.firstlab.blog.dto.CommentInfoResponse;
import xyz.firstlab.blog.dto.CommentUpdateRequest;
import xyz.firstlab.blog.dto.ResultList;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.article.ArticleRepository;
import xyz.firstlab.blog.entity.comment.Comment;
import xyz.firstlab.blog.entity.comment.CommentRepository;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    ArticleRepository articleRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CommentService commentService;

    @Test
    void postComment_Successful() {
        User testUser = createTestUser();
        Article testArticle = Mockito.spy(createTestArticle(testUser));
        when(testArticle.getId()).thenReturn(10L);

        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(10L, "This is comment's content.");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(articleRepository.findById(10L)).thenReturn(Optional.of(testArticle));

        CommentInfoResponse commentInfoResponse = commentService.postComment(1L, commentCreateRequest);

        assertThat(commentInfoResponse.content()).isEqualTo(commentCreateRequest.content());
        assertThat(commentInfoResponse.articleId()).isEqualTo(10L);
        assertThat(commentInfoResponse.username()).isEqualTo(testUser.getUsername());

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void postComment_ArticleNotExists() {
        User testUser = createTestUser();

        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(10L, "This is comment's content.");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> commentService.postComment(1L, commentCreateRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void postComment_UserNotExists() {
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(10L, "This is comment's content.");

        assertThatThrownBy(() -> commentService.postComment(1000L, commentCreateRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void readCommentsByArticle_Successful() {
        User testUser = createTestUser();
        Article testArticle = Mockito.spy(createTestArticle(testUser));
        when(testArticle.getId()).thenReturn(10L);
        Comment comment1 = createTestComment(testUser, testArticle, "Comment 1");
        Comment comment2 = createTestComment(testUser, testArticle, "Comment 2");

        when(articleRepository.existsById(10L)).thenReturn(true);
        when(commentRepository.findCommentsByArticleId(10L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(comment1, comment2)));

        ResultList<CommentInfoResponse> result = commentService.readCommentsByArticle(10L, Pageable.unpaged());

        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();

        assertThat(result.getResults()).extracting("articleId").containsExactly(10L, 10L);
        assertThat(result.getResults()).extracting("username")
                .containsExactly(testUser.getUsername(), testUser.getUsername());
        assertThat(result.getResults()).extracting("content").containsExactly("Comment 1", "Comment 2");
    }

    @Test
    void readCommentsByArticle_ArticleNotExists() {
        when(articleRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.readCommentsByArticle(10L, Pageable.unpaged()))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void updateComment_Successful() {
        User testUser = Mockito.spy(createTestUser());
        Article testArticle = Mockito.spy(createTestArticle(testUser));
        Comment comment = createTestComment(testUser, testArticle, "Comment");

        when(testUser.getId()).thenReturn(1L);
        when(testArticle.getId()).thenReturn(10L);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("update comment");

        CommentInfoResponse result = commentService.updateComment(1L, 100L, commentUpdateRequest);

        assertThat(result.articleId()).isEqualTo(10L);
        assertThat(result.username()).isEqualTo("testUser");
        assertThat(result.content()).isEqualTo("update comment");

        assertThat(comment.getContent()).isEqualTo("update comment");
    }

    @Test
    void updateComment_UserIsNotAuthorOfComment() {
        User testUser = Mockito.spy(createTestUser());
        Article testArticle = createTestArticle(testUser);
        Comment comment = createTestComment(testUser, testArticle, "Comment");

        when(testUser.getId()).thenReturn(1L);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("update comment");

        // Not matched comment's userId and the userId argument.
        assertThatThrownBy(() -> commentService.updateComment(5L, 100L, commentUpdateRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteComment_Successful() {
        User testUser = Mockito.spy(createTestUser());
        Article testArticle = Mockito.spy(createTestArticle(testUser));
        Comment comment = createTestComment(testUser, testArticle, "Comment");

        when(testUser.getId()).thenReturn(1L);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        
        commentService.deleteComment(1L, 100L);
        
        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    void deleteComment_UserIsNotAuthorOfComment() {
        User testUser = Mockito.spy(createTestUser());
        Article testArticle = createTestArticle(testUser);
        Comment comment = createTestComment(testUser, testArticle, "Comment");

        when(testUser.getId()).thenReturn(1L);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(5L, 100L))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    private static Comment createTestComment(User user, Article article, String content) {
        return Comment.builder()
                .user(user)
                .article(article)
                .content(content)
                .build();
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