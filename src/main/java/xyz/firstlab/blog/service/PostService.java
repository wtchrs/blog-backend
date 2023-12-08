package xyz.firstlab.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.PostCreateRequest;
import xyz.firstlab.blog.dto.PostInfoResponse;
import xyz.firstlab.blog.dto.PostUpdateRequest;
import xyz.firstlab.blog.entity.post.Post;
import xyz.firstlab.blog.entity.post.PostRepository;
import xyz.firstlab.blog.entity.user.User;
import xyz.firstlab.blog.entity.user.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    @Transactional
    public PostInfoResponse createPost(String username, PostCreateRequest postCreateRequest) {
        // TODO: Instead of reading the entire entity,
        //  change it to only check whether it exists and whether it has been deleted.

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User '" + username + "' is not exists.");
        }

        User user = optionalUser.get();
        if (user.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User '" + username + "' is not exists.");
        }

        Post post = postCreateRequest.toPost(user);
        postRepository.save(post);
        return PostInfoResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PostInfoResponse readPost(Long postId) {
        Post post = getPost(postId);
        post.increaseViews();
        return PostInfoResponse.from(post);
    }

    private Post getPost(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post id '" + postId + "' is not exists.");
        }

        Post post = optionalPost.get();
        if (post.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post id '" + postId + "' is not exists.");
        }

        return post;
    }

    @Transactional
    public PostInfoResponse updatePost(Long postId, Long userId, PostUpdateRequest postUpdateRequest) {
        Post post = getPost(postId);

        if (!userId.equals(post.getAuthor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author of this post can update it.");
        }

        post.update(postUpdateRequest.title(), postUpdateRequest.content());
        return PostInfoResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getPost(postId);

        if (!userId.equals(post.getAuthor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author of this post can delete it.");
        }

        post.delete();
    }

}
