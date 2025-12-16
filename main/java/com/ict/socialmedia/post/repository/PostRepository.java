package com.ict.socialmedia.post.repository;

import com.ict.socialmedia.post.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    List<Post> findAll();

    Optional<Post> findById(UUID id);

    Post create(Post post);

    boolean update(Post post);

    boolean delete(UUID id);
}


