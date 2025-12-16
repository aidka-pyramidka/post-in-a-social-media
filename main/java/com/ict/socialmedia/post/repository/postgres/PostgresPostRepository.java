package com.ict.socialmedia.post.repository.postgres;

import com.ict.socialmedia.post.model.Post;
import com.ict.socialmedia.post.model.Visibility;
import com.ict.socialmedia.post.repository.PostRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostgresPostRepository implements PostRepository {
    private final String url;
    private final String user;
    private final String password;

    public PostgresPostRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to PostgreSQL", e);
        }
    }

    @Override
    public List<Post> findAll() {
        String sql = "SELECT id, author, content, visibility, likes, created_at, updated_at FROM posts ORDER BY created_at DESC";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Post> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("findAll failed", e);
        }
    }

    @Override
    public Optional<Post> findById(UUID id) {
        String sql = "SELECT id, author, content, visibility, likes, created_at, updated_at FROM posts WHERE id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("findById failed", e);
        }
    }

    @Override
    public Post create(Post post) {
        String sql = "INSERT INTO posts (id, author, content, visibility, likes, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, post.getId());
            ps.setString(2, post.getAuthor());
            ps.setString(3, post.getContent());
            ps.setString(4, post.getVisibility().name());
            ps.setInt(5, post.getLikes());
            ps.setTimestamp(6, Timestamp.from(post.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.from(post.getUpdatedAt()));
            ps.executeUpdate();
            return post;
        } catch (Exception e) {
            throw new RuntimeException("create failed", e);
        }
    }

    @Override
    public boolean update(Post post) {
        String sql = "UPDATE posts SET author = ?, content = ?, visibility = ?, likes = ?, updated_at = ? WHERE id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, post.getAuthor());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getVisibility().name());
            ps.setInt(4, post.getLikes());
            ps.setTimestamp(5, Timestamp.from(post.getUpdatedAt()));
            ps.setObject(6, post.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("update failed", e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("delete failed", e);
        }
    }

    private static Post map(ResultSet rs) throws Exception {
        UUID id = (UUID) rs.getObject("id");
        String author = rs.getString("author");
        String content = rs.getString("content");
        Visibility visibility = Visibility.valueOf(rs.getString("visibility"));
        int likes = rs.getInt("likes");
        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        Instant updatedAt = rs.getTimestamp("updated_at").toInstant();
        return new Post(id, author, content, visibility, likes, createdAt, updatedAt);
    }
}


