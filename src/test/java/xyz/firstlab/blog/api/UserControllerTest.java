package xyz.firstlab.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import xyz.firstlab.blog.dto.SignUpRequest;
import xyz.firstlab.blog.dto.UserUpdateRequest;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;
import xyz.firstlab.blog.security.JpaUserDetails;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class UserControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

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
    void createUser() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest("test", "1234", "1234", "name", "blogName", "greeting");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf().asHeader())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) // Response status is 200 OK
                .andExpect(jsonPath(
                        "signUpDate",
                        Matchers.matchesRegex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?$")
                ))
                .andDo(document(
                        "/api/users",
                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("username"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("password"),
                                fieldWithPath("passwordConfirmation").type(JsonFieldType.STRING)
                                        .description("password confirmation"),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("name to expose other users"),
                                fieldWithPath("blogName").type(JsonFieldType.STRING).description("blog name"),
                                fieldWithPath("greeting").type(JsonFieldType.STRING).description("greeting message")
                        ),
                        getUserInfoResponseFieldsSnippet()
                ));
    }

    @Test
    @Transactional
    void getUserInfo() throws Exception {
        User user = createMockUser();
        userRepository.save(user);

        mockMvc.perform(get("/api/users/{username}", "testUser"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) // Response status is 200 OK
                .andDo(document(
                        "/api/users/{username} - GET",
                        pathParameters(parameterWithName("username").description("username to find")),
                        getUserInfoResponseFieldsSnippet()
                ));
    }

    @Test
    @Transactional
    void updateUser() throws Exception {
        User user = createMockUser();
        userRepository.save(user);
        UserDetails userDetails = new JpaUserDetails(user);

        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("new name", "new blog name", "new greeting");

        ResultActions result = mockMvc.perform(put("/api/users/{username}", "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .with(csrf().asHeader())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) // Response status is 200 OK
                .andExpectAll(
                        jsonPath("name").value(userUpdateRequest.name()),
                        jsonPath("blogName").value(userUpdateRequest.blogName()),
                        jsonPath("greeting").value(userUpdateRequest.greeting())
                );

        assertThat(user.getName()).isEqualTo(userUpdateRequest.name());
        assertThat(user.getBlogName()).isEqualTo(userUpdateRequest.blogName());
        assertThat(user.getGreeting()).isEqualTo(userUpdateRequest.greeting());

        result.andDo(document(
                "/api/users/{username} - PUT",
                pathParameters(parameterWithName("username").description("username to update")),
                getUserInfoResponseFieldsSnippet()
        ));
    }

    @Test
    @Transactional
    void deleteUser() throws Exception {
        User user = createMockUser();
        userRepository.save(user);
        UserDetails userDetails = new JpaUserDetails(user);

        ResultActions result = mockMvc.perform(delete("/api/users/{username}", "testUser")
                        .with(user(userDetails))
                        .with(csrf().asHeader())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()); // Response status is 200 OK

        assertThat(user.isDeleted()).isTrue();

        result.andDo(document(
                "/api/users/{username} - DELETE",
                pathParameters(parameterWithName("username").description("username to find")),
                responseFields(
                        fieldWithPath("message").type(JsonFieldType.STRING)
                                .description("A constant message of \"User is successfully deleted.\"")
                )
        ));
    }

    private static ResponseFieldsSnippet getUserInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("username").type(JsonFieldType.STRING)
                        .description("The username of the user who has signed up"),
                fieldWithPath("name").type(JsonFieldType.STRING)
                        .description("The name for displaying"),
                fieldWithPath("blogName").type(JsonFieldType.STRING)
                        .description("The blog name associated with the user signed up"),
                fieldWithPath("greeting").type(JsonFieldType.STRING)
                        .description("The greeting message displayed in the user page"),
                fieldWithPath("signUpDate").type(JsonFieldType.STRING)
                        .description("The timestamp indicating when the user signed up")
        );
    }

    private static User createMockUser() {
        return User.builder()
                .username("testUser")
                .password("password")
                .name("name")
                .blogName("blogName")
                .greeting("Hello!")
                .build();
    }

}
