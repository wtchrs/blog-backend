package xyz.firstlab.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.SignUpRequest;
import xyz.firstlab.blog.dto.UserInfoResponse;
import xyz.firstlab.blog.dto.UserUpdateRequest;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;
import xyz.firstlab.blog.exception.DuplicateUsernameException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void signUp_Successful() {
        SignUpRequest signUp = new SignUpRequest("testUser", "password", "password", "name", "blogName", "Hello!");

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        UserInfoResponse result = userService.signUp(signUp);

        assertThat(result.username()).isEqualTo("testUser");
        assertThat(result.name()).isEqualTo("name");
        assertThat(result.blogName()).isEqualTo("blogName");
        assertThat(result.greeting()).isEqualTo("Hello!");
    }

    @Test
    void signUp_DuplicateUsername() {
        SignUpRequest signUp = new SignUpRequest("testUser", "password", "password", "name", "blogName", "Hello!");
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        assertThatThrownBy(() -> userService.signUp(signUp))
                .isExactlyInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    void signUp_PasswordsDoNotMatch() {
        SignUpRequest signUp =
                new SignUpRequest("testUser", "password", "passwordNotMatch", "name", "blogName", "Hello!");

        assertThatThrownBy(() -> userService.signUp(signUp))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUser_Successful() {
        User testUser = createTestUser();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        UserInfoResponse result = userService.getUserInfo("testUser");

        assertUserInfoResponseEqualsUser(result, testUser);
    }

    @Test
    void getUser_UserNotExists() {
        when(userRepository.findByUsername("notExists")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo("notExists"))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void getUser_DeletedUser() {
        User testUser = createTestUser();
        testUser.delete();
        when(userRepository.findByUsername("deleted")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.getUserInfo("deleted"))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void update_Successful() {
        User testUser = createTestUser();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        UserUpdateRequest update = new UserUpdateRequest("new name", "new blog name", "new greeting");

        UserInfoResponse result = userService.update("testUser", update);

        assertThat(testUser.getName()).isEqualTo(update.name());
        assertThat(testUser.getBlogName()).isEqualTo(update.blogName());
        assertThat(testUser.getGreeting()).isEqualTo(update.greeting());

        assertUserInfoResponseEqualsUser(result, testUser);
    }

    @Test
    void update_UserNotExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        UserUpdateRequest update = new UserUpdateRequest("new name", "new blog name", "new greeting");

        assertThatThrownBy(() -> userService.update("testUser", update))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_Successful() {
        User testUser = createTestUser();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        userService.delete("testUser");

        assertThat(testUser.isDeleted()).isTrue();
    }

    @Test
    void delete_UserNotExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete("testUser"))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
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

    private static void assertUserInfoResponseEqualsUser(UserInfoResponse result, User mockUser) {
        assertThat(result.username()).isEqualTo(mockUser.getUsername());
        assertThat(result.name()).isEqualTo(mockUser.getName());
        assertThat(result.blogName()).isEqualTo(mockUser.getBlogName());
        assertThat(result.greeting()).isEqualTo(mockUser.getGreeting());
    }

}