package xyz.firstlab.blog.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.SignInRequest;
import xyz.firstlab.blog.dto.SignUpRequest;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;
import xyz.firstlab.blog.exception.DuplicateUsernameException;
import xyz.firstlab.blog.security.JpaUserDetails;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final LogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    @Transactional
    public User signUp(SignUpRequest signUp) {
        String username = signUp.username();

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException("Username '" + username + "' already exists.");
        }

        if (!signUp.password().equals(signUp.passwordConfirm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        User user = new User(username, passwordEncoder.encode(signUp.password()), username, username, "");
        userRepository.save(user);

        return user;
    }

    @Transactional(readOnly = true)
    public User signIn(HttpServletRequest request, SignInRequest signIn) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signIn.username(), signIn.password()));

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authenticate);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", context);

        JpaUserDetails userDetails = (JpaUserDetails) authenticate.getPrincipal();
        return userDetails.getUser();
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            logoutHandler.logout(request, response, authentication);
        }
    }

}
