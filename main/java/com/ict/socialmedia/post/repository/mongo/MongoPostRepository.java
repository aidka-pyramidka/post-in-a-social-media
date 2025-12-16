package com.ict.socialmedia.post.repository.mongo;

import com.ict.socialmedia.post.model.Post;
import com.ict.socialmedia.post.model.Visibility;
import com.ict.socialmedia.post.repository.PostRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Sorts.descending;

public class MongoPostRepository implements PostRepository, AutoCloseable {
    private final MongoClient client;
    private final MongoCollection<Document> collection;

    public MongoPostRepository(String connectionString, String database, String collectionName) {
        this.client = MongoClients.create(connectionString);
        MongoDatabase db = client.getDatabase(database);
        this.collection = db.getCollection(collectionName);
    }

    public MongoClient getClient() {
        return client;
    }

    @Override
    public List<Post> findAll() {
        List<Post> out = new ArrayList<>();
        for (Document d : collection.find().sort(descending("createdAt"))) {
            out.add(map(d));
        }
        return out;
    }

    @Override
    public Optional<Post> findById(UUID id) {
        Document d = collection.find(Filters.eq("_id", id.toString())).first();
        return d == null ? Optional.empty() : Optional.of(map(d));
    }

    @Override
    public Post create(Post post) {
        collection.insertOne(toDoc(post));
        return post;
    }

    @Override
    public boolean update(Post post) {
        ReplaceOptions opts = new ReplaceOptions().upsert(false);
        return collection.replaceOne(Filters.eq("_id", post.getId().toString()), toDoc(post), opts).getModifiedCount() > 0;
    }

    @Override
    public boolean delete(UUID id) {
        return collection.deleteOne(Filters.eq("_id", id.toString())).getDeletedCount() > 0;
    }

    private static Document toDoc(Post p) {
        return new Document("_id", p.getId().toString())
                .append("author", p.getAuthor())
                .append("content", p.getContent())
                .append("visibility", p.getVisibility().name())
                .append("likes", p.getLikes())
                .append("createdAt", Date.from(p.getCreatedAt()))
                .append("updatedAt", Date.from(p.getUpdatedAt()));
    }

    private static Post map(Document d) {
        UUID id = UUID.fromString(d.getString("_id"));
        String author = d.getString("author");
        String content = d.getString("content");
        Visibility visibility = Visibility.valueOf(d.getString("visibility"));
        int likes = d.getInteger("likes", 0);
        Instant createdAt = d.getDate("createdAt").toInstant();
        Instant updatedAt = d.getDate("updatedAt").toInstant();
        return new Post(id, author, content, visibility, likes, createdAt, updatedAt);
    }

    @Override
    public void close() {
        client.close();
    }
}


