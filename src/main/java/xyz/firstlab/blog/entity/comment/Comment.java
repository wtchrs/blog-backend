package xyz.firstlab.blog.entity.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private User user;

}
