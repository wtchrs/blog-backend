package xyz.firstlab.blog.entity.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.firstlab.blog.entity.BaseEntity;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.user.User;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String content;

    private boolean deleted;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private User user;

    @Builder
    public Comment(String content, Article article, User user) {
        this.content = content;
        this.article = article;
        this.user = user;
        this.deleted = false;
    }

    public void update(String newContent) {
        this.content = newContent;
    }

    public void delete() {
        this.deleted = true;
    }

}
