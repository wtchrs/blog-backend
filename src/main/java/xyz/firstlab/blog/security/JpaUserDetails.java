package xyz.firstlab.blog.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xyz.firstlab.blog.entity.user.User;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JpaUserDetails implements UserDetails {

    @Getter
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("user.getAuthority() = {}", user.getAuthority());
        return List.of(new SimpleGrantedAuthority(user.getAuthority()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !user.getDeleted();
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !user.getDeleted();
    }

}
