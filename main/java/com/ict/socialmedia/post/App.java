package com.ict.socialmedia.post;

import com.ict.socialmedia.post.config.AppConfig;
import com.ict.socialmedia.post.model.Post;
import com.ict.socialmedia.post.model.Visibility;
import com.ict.socialmedia.post.repository.PostRepository;
import com.ict.socialmedia.post.repository.mongo.MongoBootstrapper;
import com.ict.socialmedia.post.repository.mongo.MongoPostRepository;
import com.ict.socialmedia.post.repository.postgres.PostgresPostRepository;
import com.ict.socialmedia.post.repository.postgres.PostgresBootstrapper;
import com.ict.socialmedia.post.service.PostService;
import com.ict.socialmedia.post.util.ConsoleIO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        AppConfig cfg = AppConfig.loadFromClasspath("application.properties");
        PostRepository repo = buildRepository(cfg);
        PostService service = new PostService(repo);
        ConsoleIO io = new ConsoleIO();

        System.out.println("=== Post Repository App (Java) ===");
        System.out.println("Repository: " + repo.getClass().getSimpleName());
        System.out.println("Commands: list | get | create | update | delete | help | exit");

        while (true) {
            String cmd = io.ask("\n> ").trim().toLowerCase();
            try {
                switch (cmd) {
                    case "list" -> {
                        List<Post> posts = service.list();
                        if (posts.isEmpty()) {
                            System.out.println("(empty)");
                        } else {
                            posts.forEach(System.out::println);
                        }
                    }
                    case "get" -> {
                        UUID id = UUID.fromString(io.ask("id (uuid): ").trim());
                        Optional<Post> p = service.get(id);
                        System.out.println(p.map(Object::toString).orElse("(not found)"));
                    }
                    case "create" -> {
                        String author = io.ask("author: ").trim();
                        String content = io.ask("content: ").trim();
                        Visibility visibility = Visibility.parseOrDefault(io.ask("visibility (PUBLIC/FRIENDS/PRIVATE) [PUBLIC]: "), Visibility.PUBLIC);
                        int likes = io.askInt("likes [0]: ", 0);
                        Post p = service.create(author, content, visibility, likes);
                        System.out.println("Created: " + p);
                    }
                    case "update" -> {
                        UUID id = UUID.fromString(io.ask("id (uuid): ").trim());
                        Optional<Post> existing = service.get(id);
                        if (existing.isEmpty()) {
                            System.out.println("(not found)");
                            break;
                        }
                        Post old = existing.get();
                        String author = io.ask("author [" + old.getAuthor() + "]: ").trim();
                        if (author.isBlank()) author = old.getAuthor();
                        String content = io.ask("content [" + preview(old.getContent()) + "]: ").trim();
                        if (content.isBlank()) content = old.getContent();
                        Visibility visibility = Visibility.parseOrDefault(
                                io.ask("visibility (PUBLIC/FRIENDS/PRIVATE) [" + old.getVisibility() + "]: "),
                                old.getVisibility()
                        );
                        int likes = io.askInt("likes [" + old.getLikes() + "]: ", old.getLikes());
                        boolean ok = service.update(id, author, content, visibility, likes);
                        System.out.println(ok ? "Updated." : "Update failed.");
                    }
                    case "delete" -> {
                        UUID id = UUID.fromString(io.ask("id (uuid): ").trim());
                        boolean ok = service.delete(id);
                        System.out.println(ok ? "Deleted." : "(not found)");
                    }
                    case "help" -> {
                        System.out.println("Commands: list | get | create | update | delete | help | exit");
                    }
                    case "exit", "quit" -> {
                        closeIfNeeded(repo);
                        System.out.println("Bye.");
                        return;
                    }
                    default -> System.out.println("Unknown command. Type 'help'.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static PostRepository buildRepository(AppConfig cfg) {
        String type = cfg.getOptional("repository.type", "postgres").toLowerCase();
        return switch (type) {
            case "postgres", "postgresql" -> {
                String url = cfg.getRequired("postgres.url");
                String user = cfg.getRequired("postgres.user");
                String password = cfg.getRequired("postgres.password");
                String adminDb = cfg.getOptional("postgres.adminDb", "postgres");

                boolean bootstrapEnabled = Boolean.parseBoolean(cfg.getOptional("postgres.bootstrap.enabled", "true"));
                boolean bootstrapSeed = Boolean.parseBoolean(cfg.getOptional("postgres.bootstrap.seed", "false"));

                PostgresBootstrapper.bootstrapIfEnabled(
                        bootstrapEnabled,
                        url,
                        adminDb,
                        user,
                        password,
                        bootstrapSeed
                );

                yield new PostgresPostRepository(url, user, password);
            }
            case "mongo", "mongodb" -> {
                String cs = cfg.getRequired("mongo.connectionString");
                String db = cfg.getRequired("mongo.database");
                String col = cfg.getRequired("mongo.collection");

                MongoPostRepository repo = new MongoPostRepository(cs, db, col);
                boolean mongoBootstrapEnabled = Boolean.parseBoolean(cfg.getOptional("mongo.bootstrap.enabled", "true"));
                boolean mongoBootstrapSeed = Boolean.parseBoolean(cfg.getOptional("mongo.bootstrap.seed", "true"));
                MongoBootstrapper.bootstrapIfEnabled(
                        mongoBootstrapEnabled,
                        repo.getClient(),
                        db,
                        col,
                        mongoBootstrapSeed
                );
                yield repo;
            }
            default -> throw new IllegalArgumentException("Unknown repository.type: " + type);
        };
    }

    private static void closeIfNeeded(PostRepository repo) {
        if (repo instanceof AutoCloseable closable) {
            try {
                closable.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static String preview(String content) {
        if (content == null) return "";
        return content.length() > 24 ? content.substring(0, 24) + "..." : content;
    }
}


