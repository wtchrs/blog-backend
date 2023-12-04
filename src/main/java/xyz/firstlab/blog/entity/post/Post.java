package xyz.firstlab.blog.entity.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.firstlab.blog.entity.BaseEntity;
import xyz.firstlab.blog.entity.comment.Comment;
import xyz.firstlab.blog.entity.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private User author;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    public Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

}
