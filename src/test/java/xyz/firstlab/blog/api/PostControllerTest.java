package xyz.firstlab.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import xyz.firstlab.blog.dto.PostCreateRequest;
import xyz.firstlab.blog.dto.PostUpdateRequest;
import xyz.firstlab.blog.entity.post.Post;
import xyz.firstlab.blog.entity.post.PostRepository;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;
import xyz.firstlab.blog.security.JpaUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class PostControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    void settingMockMvc(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }

    @Test
    @Transactional
    void createPost() throws Exception {
        PostCreateRequest postCreateRequest = new PostCreateRequest("test title", "This is content.");
        User testUser = createTestUser();
        userRepository.save(testUser);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCreateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf().asHeader())
                        .with(user(new JpaUserDetails(testUser)))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) // Response status is 200 OK
                .andExpectAll(
                        jsonPath(
                                "createdAt",
                                Matchers.matchesRegex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?$")
                        ),
                        jsonPath(
                                "modifiedAt",
                                Matchers.matchesRegex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?$")
                        )
                )
                .andDo(document(
                        "/api/posts - POST",
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("title"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("content")
                        ),
                        getPostInfoResponseFieldsSnippet()
                ));
    }

    @Test
    @Transactional
    void readPost() throws Exception {
        User testUser = createTestUser();
        Post testPost = createTestPost(testUser);
        userRepository.save(testUser);
        postRepository.save(testPost);

        mockMvc.perform(get("/api/posts/{postId}", testPost.getId())
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) // Response status is 200 OK
                .andDo(document(
                        "/api/posts/{postId} - GET",
                        pathParameters(parameterWithName("postId").description("identity of the post to find")),
                        getPostInfoResponseFieldsSnippet()
                ));
    }

    @Test
    @Transactional
    void updatePost() throws Exception {
        User testUser = createTestUser();
        Post testPost = createTestPost(testUser);
        userRepository.save(testUser);
        postRepository.save(testPost);

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("new title", "new content");

        ResultActions resultActions = mockMvc.perform(put("/api/posts/{postId}", testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf().asHeader())
                        .with(user(new JpaUserDetails(testUser)))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()); // Response status is 200 OK

        assertThat(testPost.getTitle()).isEqualTo(postUpdateRequest.title());
        assertThat(testPost.getContent()).isEqualTo(postUpdateRequest.content());

        resultActions.andDo(document(
                "/api/posts/{postId} - PUT",
                pathParameters(parameterWithName("postId").description("identity of the post to update")),
                getPostInfoResponseFieldsSnippet()
        ));
    }

    @Test
    @Transactional
    void deletePost() throws Exception {
        User testUser = createTestUser();
        Post testPost = createTestPost(testUser);
        userRepository.save(testUser);
        postRepository.save(testPost);

        ResultActions resultActions = mockMvc.perform(delete("/api/posts/{postId}", testPost.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf().asHeader())
                        .with(user(new JpaUserDetails(testUser)))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()); // Response status is 200 OK

        assertThat(testPost.isDeleted()).isTrue();

        resultActions.andDo(document(
                "/api/posts/{postId} - DELETE",
                pathParameters(parameterWithName("postId").description("identity of the post to delete")),
                responseFields(fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("A constant message of \\\"Post is successfully deleted.\\\""))
        ));
    }

    private static ResponseFieldsSnippet getPostInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("postId").type(JsonFieldType.NUMBER).description("post id"),
                fieldWithPath("username").type(JsonFieldType.STRING).description("username of author"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("title of the post"),
                fieldWithPath("content").type(JsonFieldType.STRING).description("content of the post"),
                fieldWithPath("views").type(JsonFieldType.NUMBER).description("views of the post"),
                fieldWithPath("createdAt").type(JsonFieldType.STRING)
                        .description("timestamp when the post was created"),
                fieldWithPath("modifiedAt").type(JsonFieldType.STRING)
                        .description("timestamp when the post was last modified ")
        );
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