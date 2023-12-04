package xyz.firstlab.blog.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import xyz.firstlab.blog.entity.BaseEntity;

@Entity
@Getter
public class Follow extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "following_user_id", nullable = false, updatable = false)
    private User following;

    @ManyToOne
    @JoinColumn(name = "followed_user_id", nullable = false, updatable = false)
    private User followed;

}
