package xyz.firstlab.blog.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import xyz.firstlab.blog.dto.MessageResponse;

@RestController
@RequestMapping("/api")
public class CsrfTokenController {

    @GetMapping("/csrf")
    public MessageResponse csrf(CsrfToken csrfToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("XSRF-TOKEN", csrfToken.getToken());
        response.addCookie(cookie);
        return new MessageResponse("The 'XSRF-TOKEN' cookie contains new CSRF token value.");
    }

}
