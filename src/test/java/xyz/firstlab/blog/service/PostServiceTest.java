package xyz.firstlab.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.PostCreateRequest;
import xyz.firstlab.blog.dto.PostInfoResponse;
import xyz.firstlab.blog.dto.PostUpdateRequest;
import xyz.firstlab.blog.entity.post.Post;
import xyz.firstlab.blog.entity.post.PostRepository;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    PostService postService;

    @Test
    void createPost_Success() {
        User testUser = createTestUser();

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        PostCreateRequest postCreateRequest = new PostCreateRequest("test title", "This is content.");
        PostInfoResponse result = postService.createPost("testUser", postCreateRequest);

        assertThat(result.title()).isEqualTo(postCreateRequest.title());
        assertThat(result.content()).isEqualTo(postCreateRequest.content());
        assertThat(result.views()).isEqualTo(0);
        assertThat(result.username()).isEqualTo("testUser");

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void createPost_UserNotExists() {
        PostCreateRequest postCreateRequest = new PostCreateRequest("test title", "This is content.");

        assertThatThrownBy(() -> postService.createPost("testUser", postCreateRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void readPost_Successful() {
        User testUser = createTestUser();
        Post mockPost = Mockito.spy(createTestPost(testUser));
        when(mockPost.getId()).thenReturn(10L);

        when(postRepository.findById(10L)).thenReturn(Optional.of(mockPost));

        PostInfoResponse result = postService.readPost(10L);

        assertThat(result.postId()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo(mockPost.getTitle());
        assertThat(result.content()).isEqualTo(mockPost.getContent());
        assertThat(result.username()).isEqualTo(mockPost.getAuthor().getUsername());

        verify(mockPost, times(1)).increaseViews();
    }

    @Test
    void readPost_PostNotExists() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.readPost(10L))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void updatePost_Successful() {
        User testUser = Mockito.spy(createTestUser());
        when(testUser.getId()).thenReturn(1L);
        Post testPost = createTestPost(testUser);

        when(postRepository.findById(10L)).thenReturn(Optional.of(testPost));

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("new title", "new content");
        PostInfoResponse result = postService.updatePost(10L, testUser.getId(), postUpdateRequest);

        assertThat(testPost.getTitle()).isEqualTo(postUpdateRequest.title());
        assertThat(testPost.getContent()).isEqualTo(postUpdateRequest.content());
        assertThat(testPost.getAuthor()).isSameAs(testUser);

        assertThat(result.title()).isEqualTo(postUpdateRequest.title());
        assertThat(result.content()).isEqualTo(postUpdateRequest.content());
        assertThat(result.username()).isEqualTo(testPost.getAuthor().getUsername());
    }

    @Test
    void updatePost_PostNotExists() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("new title", "new content");

        assertThatThrownBy(() -> postService.updatePost(10L, 1L, postUpdateRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePost_Successful() {
        User testUser = Mockito.spy(createTestUser());
        when(testUser.getId()).thenReturn(1L);
        Post testPost = createTestPost(testUser);

        when(postRepository.findById(10L)).thenReturn(Optional.of(testPost));

        postService.deletePost(10L, testUser.getId());

        assertThat(testPost.isDeleted()).isTrue();
    }

    @Test
    void deletePost_PostNotExists() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(10L, 1L))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    private static Post createTestPost(User user) {
        return Post.builder()
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