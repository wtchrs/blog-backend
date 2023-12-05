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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import xyz.firstlab.blog.dto.SignUpRequest;
import xyz.firstlab.blog.entity.user.UserRepository;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .build();
    }

    @Test
    @Transactional
    void signUpShouldResponseUsernameAndSignUpDate() throws Exception {
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
                        "/api/auth/sign-up",
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
                        responseFields(
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
                        )
                ));
    }

}