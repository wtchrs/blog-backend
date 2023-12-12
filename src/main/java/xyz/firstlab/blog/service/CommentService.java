package xyz.firstlab.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.CommentCreateRequest;
import xyz.firstlab.blog.dto.CommentInfoResponse;
import xyz.firstlab.blog.dto.CommentUpdateRequest;
import xyz.firstlab.blog.dto.ResultList;
import xyz.firstlab.blog.entity.article.Article;
import xyz.firstlab.blog.entity.article.ArticleRepository;
import xyz.firstlab.blog.entity.comment.Comment;
import xyz.firstlab.blog.entity.comment.CommentRepository;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    // TODO: look up comments by user.

    @Transactional
    public CommentInfoResponse postComment(Long userId, CommentCreateRequest commentCreateRequest) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not exists.");
        }

        User user = optionalUser.get();
        if (user.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not exists.");
        }

        Long articleId = commentCreateRequest.articleId();

        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article is not exists.");
        }

        Article article = optionalArticle.get();
        if (article.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article is not exists.");
        }

        Comment comment = commentCreateRequest.toComment(user, article);
        commentRepository.save(comment);
        return CommentInfoResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public ResultList<CommentInfoResponse> readCommentsByArticle(Long articleId, Pageable pageable) {
        if (!articleRepository.existsById(articleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not exists.");
        }

        Slice<Comment> comments = commentRepository.findCommentsByArticleId(articleId, pageable);
        Slice<CommentInfoResponse> dtoSlice = comments.map(CommentInfoResponse::from);
        return ResultList.from(dtoSlice);
    }

    @Transactional
    public CommentInfoResponse updateComment(Long userId, Long commentId, CommentUpdateRequest commentUpdateRequest) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not exists.");
        }

        Comment comment = optionalComment.get();
        if (!userId.equals(comment.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author of this comment can update it.");
        }

        comment.update(commentUpdateRequest.content());
        return CommentInfoResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not exists.");
        }

        Comment comment = optionalComment.get();
        if (!userId.equals(comment.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author of this comment can delete it.");
        }

        comment.delete();
    }

}
