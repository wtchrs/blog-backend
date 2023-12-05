package xyz.firstlab.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import xyz.firstlab.blog.dto.SignInRequest;
import xyz.firstlab.blog.dto.SignUpRequest;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class AuthControllerTest {

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
                .build();
    }

    @Test
    @Transactional
    void signInShouldResponseNameAndBlogName() throws Exception {
        User user = new User("test", passwordEncoder.encode("1234"), "Test", "Test Blog", "greeting");
        userRepository.save(user);

        SignInRequest test = new SignInRequest("test", "1234");

        MockHttpSession session = new MockHttpSession();

        ResultActions result = mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test))
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .with(csrf().asHeader())
        );

        assertThat(session.getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();

        SecurityContext context = TestSecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNotNull();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document(
                        "/api/auth/sign-in",
                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("username"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("password")
                        ),
                        responseFields(
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("The displayed name of the signed-in user"),
                                fieldWithPath("blogName").type(JsonFieldType.STRING)
                                        .description("The blog name associated with the signed-in user")
                        )
                ));
    }

    @Test
    @Transactional
    void signUpShouldResponseUsernameAndSignUpDate() throws Exception {
        SignUpRequest test = new SignUpRequest("test", "1234", "1234");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(test))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf().asHeader())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath(
                                "signUpDate",
                                Matchers.matchesRegex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?$")
                        )
                )
                .andDo(document(
                        "/api/auth/sign-up",
                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("username"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("password"),
                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING)
                                        .description("password confirm")
                        ),
                        responseFields(
                                fieldWithPath("username").type(JsonFieldType.STRING)
                                        .description("The username of the user who has signed up"),
                                fieldWithPath("signUpDate").type(JsonFieldType.STRING)
                                        .description("The timestamp indicating when the user signed up")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "test")
    void signOutShouldRemoveSessionInformation() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/auth/sign-out").with(csrf().asHeader()));

        MvcResult mvcResult = result.andReturn();
        HttpSession session = mvcResult.getRequest().getSession(false);
        assertThat(session).isNull();

        result.andDo(MockMvcResultHandlers.print())
                .andDo(document(
                        "/api/auth/sign-out",
                        responseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("A constant message of \"Successfully signed out.\"")
                        )
                ));
    }

}
