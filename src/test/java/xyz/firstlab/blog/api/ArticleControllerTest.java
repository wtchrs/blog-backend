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
import xyz.firstlab.blog.dto.ArticleCreateRequest;
import xyz.firstlab.blog.dto.ArticleUpdateRequest;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.article.ArticleRepository;
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
class ArticleControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;

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
        ArticleCreateRequest articleCreateRequest = new ArticleCreateRequest("test title", "This is content.");
        User testUser = createTestUser();
        userRepository.save(testUser);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleCreateRequest))
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
                        "/api/articles - POST",
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
        Article testArticle = createTestPost(testUser);
        userRepository.save(testUser);
        articleRepository.save(testArticle);

        mockMvc.perform(get("/api/articles/{articleId}", testArticle.getId())
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) // Response status is 200 OK
                .andDo(document(
                        "/api/articles/{articleId} - GET",
                        pathParameters(parameterWithName("articleId").description("identity of the article to find")),
                        getPostInfoResponseFieldsSnippet()
                ));
    }

    @Test
    @Transactional
    void updatePost() throws Exception {
        User testUser = createTestUser();
        Article testArticle = createTestPost(testUser);
        userRepository.save(testUser);
        articleRepository.save(testArticle);

        ArticleUpdateRequest articleUpdateRequest = new ArticleUpdateRequest("new title", "new content");

        ResultActions resultActions = mockMvc.perform(put("/api/articles/{articleId}", testArticle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleUpdateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf().asHeader())
                        .with(user(new JpaUserDetails(testUser)))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()); // Response status is 200 OK

        assertThat(testArticle.getTitle()).isEqualTo(articleUpdateRequest.title());
        assertThat(testArticle.getContent()).isEqualTo(articleUpdateRequest.content());

        resultActions.andDo(document(
                "/api/articles/{articleId} - PUT",
                pathParameters(parameterWithName("articleId").description("identity of the article to update")),
                getPostInfoResponseFieldsSnippet()
        ));
    }

    @Test
    @Transactional
    void deletePost() throws Exception {
        User testUser = createTestUser();
        Article testArticle = createTestPost(testUser);
        userRepository.save(testUser);
        articleRepository.save(testArticle);

        ResultActions resultActions = mockMvc.perform(delete("/api/articles/{articleId}", testArticle.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf().asHeader())
                        .with(user(new JpaUserDetails(testUser)))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()); // Response status is 200 OK

        assertThat(testArticle.isDeleted()).isTrue();

        resultActions.andDo(document(
                "/api/articles/{articleId} - DELETE",
                pathParameters(parameterWithName("articleId").description("identity of the article to delete")),
                responseFields(fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("A constant message of \\\"Post is successfully deleted.\\\""))
        ));
    }

    private static ResponseFieldsSnippet getPostInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("articleId").type(JsonFieldType.NUMBER).description("article id"),
                fieldWithPath("username").type(JsonFieldType.STRING).description("username of author"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("title of the article"),
                fieldWithPath("content").type(JsonFieldType.STRING).description("content of the article"),
                fieldWithPath("views").type(JsonFieldType.NUMBER).description("views of the article"),
                fieldWithPath("createdAt").type(JsonFieldType.STRING)
                        .description("timestamp when the article was created"),
                fieldWithPath("modifiedAt").type(JsonFieldType.STRING)
                        .description("timestamp when the article was last modified ")
        );
    }

    private static Article createTestPost(User user) {
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