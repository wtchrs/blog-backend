package xyz.firstlab.blog.entity.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import xyz.firstlab.blog.entity.BaseEntity;
import xyz.firstlab.blog.entity.comment.Comment;
import xyz.firstlab.blog.entity.post.Post;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String blogName;

    private String greeting;

    @Column(nullable = false)
    private String authority;

    private boolean deleted;

    @OneToMany(mappedBy = "following")
    private List<Follow> followings = new ArrayList<>();

    @OneToMany(mappedBy = "followed")
    private List<Follow> followers = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public User(String username, String password, String name, String blogName, String greeting) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.blogName = blogName;
        this.greeting = greeting;
        this.authority = "ROLE_USER";
        this.deleted = false;
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void update(String name, String blogName, String greeting) {
        this.name = name;
        this.blogName = blogName;
        this.greeting = greeting;
    }

    public void delete() {
        this.deleted = true;
    }

}
