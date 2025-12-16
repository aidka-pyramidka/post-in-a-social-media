package com.ict.socialmedia.post.service;

import com.ict.socialmedia.post.model.Post;
import com.ict.socialmedia.post.model.Visibility;
import com.ict.socialmedia.post.repository.PostRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostService {
    private final PostRepository repo;

    public PostService(PostRepository repo) {
        this.repo = repo;
    }

    public List<Post> list() {
        return repo.findAll();
    }

    public Optional<Post> get(UUID id) {
        return repo.findById(id);
    }

    public Post create(String author, String content, Visibility visibility, int likes) {
        Instant now = Instant.now();
        Post p = new Post(UUID.randomUUID(), author, content, visibility, likes, now, now);
        return repo.create(p);
    }

    public boolean update(UUID id, String author, String content, Visibility visibility, int likes) {
        Optional<Post> existing = repo.findById(id);
        if (existing.isEmpty()) return false;
        Post updated = existing.get().withUpdatedFields(author, content, visibility, likes, Instant.now());
        return repo.update(updated);
    }

    public boolean delete(UUID id) {
        return repo.delete(id);
    }
}


