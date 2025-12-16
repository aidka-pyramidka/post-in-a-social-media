package com.ict.socialmedia.post.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Post {
    private final UUID id;
    private final String author;
    private final String content;
    private final Visibility visibility;
    private final int likes;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Post(
            UUID id,
            String author,
            String content,
            Visibility visibility,
            int likes,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.author = Objects.requireNonNull(author, "author");
        this.content = Objects.requireNonNull(content, "content");
        this.visibility = Objects.requireNonNull(visibility, "visibility");
        if (likes < 0) throw new IllegalArgumentException("likes must be >= 0");
        this.likes = likes;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public UUID getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public int getLikes() {
        return likes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Post withUpdatedFields(String author, String content, Visibility visibility, int likes, Instant updatedAt) {
        return new Post(this.id, author, content, visibility, likes, this.createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", visibility=" + visibility +
                ", likes=" + likes +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", content='" + (content.length() > 60 ? content.substring(0, 60) + "..." : content) + '\'' +
                '}';
    }
}


