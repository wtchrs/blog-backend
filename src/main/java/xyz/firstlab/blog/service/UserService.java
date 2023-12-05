package xyz.firstlab.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.SignUpRequest;
import xyz.firstlab.blog.dto.UserInfoResponse;
import xyz.firstlab.blog.dto.UserUpdateRequest;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;
import xyz.firstlab.blog.exception.DuplicateUsernameException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserInfoResponse signUp(SignUpRequest signUp) {
        if (!signUp.password().equals(signUp.passwordConfirmation())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        if (userRepository.existsByUsername(signUp.username())) {
            throw new DuplicateUsernameException("Username '" + signUp.username() + "' already exists.");
        }

        User user = signUp.toUser();
        user.encodePassword(passwordEncoder);
        userRepository.save(user);

        return UserInfoResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String username) {
        User user = getUser(username);
        return UserInfoResponse.from(user);
    }

    private User getUser(String username) {
        Optional<User> optional = userRepository.findByUsername(username);
        if (optional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User '" + username + "' is not exists.");
        }

        User user = optional.get();
        if (user.getDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User '" + username + "' is not exists.");
        }

        return user;
    }

    @Transactional
    public UserInfoResponse update(String username, UserUpdateRequest userUpdate) {
        User user = getUser(username);
        user.update(userUpdate.name(), userUpdate.blogName(), userUpdate.greeting());
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void delete(String username) {
        User user = getUser(username);
        user.delete();
    }

}
