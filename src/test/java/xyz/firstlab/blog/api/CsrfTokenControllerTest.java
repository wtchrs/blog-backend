package xyz.firstlab.blog.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class CsrfTokenControllerTest {

    MockMvc mockMvc;

    @BeforeEach
    void settingMockMvc(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }

    @Test
    void csrfShouldResponseWithCookieValue() throws Exception {
        CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "CSRF_TOKEN_VALUE_HERE");

        mockMvc.perform(get("/api/csrf").requestAttr(CsrfToken.class.getName(), csrfToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) // Response status is 200 OK
                .andExpectAll(
                        jsonPath("message").value("The 'XSRF-TOKEN' cookie contains new CSRF token value."),
                        cookie().exists("XSRF-TOKEN")
                )
                .andDo(MockMvcRestDocumentation.document(
                        "/api/csrf",
                        responseFields(fieldWithPath("message").description(
                                "A constant message of \"The 'XSRF-TOKEN' cookie contains new CSRF token value.\"")),
                        responseCookies(cookieWithName("XSRF-TOKEN").description("The new CSRF token value"))
                ));
    }

}
