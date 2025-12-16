package com.ict.socialmedia.post.repository.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Date;
import java.util.Objects;

public final class MongoBootstrapper {
    private MongoBootstrapper() {
    }

    public static void bootstrapIfEnabled(
            boolean enabled,
            MongoClient client,
            String database,
            String collectionName,
            boolean seed
    ) {
        if (!enabled) return;

        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(database, "database");
        Objects.requireNonNull(collectionName, "collectionName");

        MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collectionName);

        col.createIndex(Indexes.descending("createdAt"));
        col.createIndex(Indexes.ascending("author"));

        if (!seed) return;

        upsertSeed(col,
                "11111111-1111-1111-1111-111111111111",
                "alice",
                "Hello, world! This is my first post.",
                "PUBLIC",
                3
        );
        upsertSeed(col,
                "22222222-2222-2222-2222-222222222222",
                "bob",
                "Studying ICT repository pattern today.",
                "FRIENDS",
                1
        );
        upsertSeed(col,
                "33333333-3333-3333-3333-333333333333",
                "carol",
                "Private note: This is is some prviate post.",
                "PRIVATE",
                0
        );
    }

    private static void upsertSeed(
            MongoCollection<Document> col,
            String id,
            String author,
            String content,
            String visibility,
            int likes
    ) {
        Date now = new Date();
        Document onInsert = new Document("_id", id)
                .append("author", author)
                .append("content", content)
                .append("visibility", visibility)
                .append("likes", likes)
                .append("createdAt", now)
                .append("updatedAt", now);

        col.updateOne(
                Filters.eq("_id", id),
                new Document("$setOnInsert", onInsert),
                new UpdateOptions().upsert(true)
        );
    }
}


