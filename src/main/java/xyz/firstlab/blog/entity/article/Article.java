package xyz.firstlab.blog.entity.article;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.firstlab.blog.entity.BaseEntity;
import xyz.firstlab.blog.entity.comment.Comment;
import xyz.firstlab.blog.entity.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int views;

    private boolean deleted;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private User author;

    @OneToMany(mappedBy = "article")
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Article(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.views = 0;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void increaseViews() {
        this.views++;
    }

    public void delete() {
        this.deleted = true;
    }

}
