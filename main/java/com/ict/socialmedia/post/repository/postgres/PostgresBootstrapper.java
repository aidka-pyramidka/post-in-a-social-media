package com.ict.socialmedia.post.repository.postgres;

import com.ict.socialmedia.post.util.SqlScriptRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.regex.Pattern;

public final class PostgresBootstrapper {
    private static final Pattern SAFE_DB_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    private PostgresBootstrapper() {
    }

    public static void bootstrapIfEnabled(
            boolean enabled,
            String jdbcUrl,
            String adminDb,
            String user,
            String password,
            boolean runSeed
    ) {
        if (!enabled) return;

        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(adminDb, "adminDb");
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(password, "password");

        String targetDb = extractDatabaseName(jdbcUrl);
        if (!SAFE_DB_NAME.matcher(targetDb).matches()) {
            throw new IllegalArgumentException("Unsafe database name in JDBC url: " + targetDb);
        }

        String adminUrl = replaceDatabaseName(jdbcUrl, adminDb);

        try (Connection c = DriverManager.getConnection(adminUrl, user, password)) {
            if (!databaseExists(c, targetDb)) {
                c.createStatement().execute("CREATE DATABASE " + targetDb);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to bootstrap database (create DB)", e);
        }

        try (Connection c = DriverManager.getConnection(jdbcUrl, user, password)) {
            SqlScriptRunner.runClasspathResource(c, "db/postgres/schema.sql");
            if (runSeed) {
                SqlScriptRunner.runClasspathResource(c, "db/postgres/seed.sql");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to bootstrap database (run schema/seed)", e);
        }
    }

    private static boolean databaseExists(Connection c, String dbName) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
            ps.setString(1, dbName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static String extractDatabaseName(String jdbcUrl) {
        String withoutPrefix = jdbcUrl;
        int prefixIdx = withoutPrefix.indexOf("jdbc:postgresql://");
        if (prefixIdx != -1) {
            withoutPrefix = withoutPrefix.substring("jdbc:postgresql://".length());
        }
        int slash = withoutPrefix.indexOf('/');
        if (slash == -1 || slash == withoutPrefix.length() - 1) {
            throw new IllegalArgumentException("JDBC url must include database name: " + jdbcUrl);
        }
        String pathAndQuery = withoutPrefix.substring(slash + 1);
        int q = pathAndQuery.indexOf('?');
        String db = q == -1 ? pathAndQuery : pathAndQuery.substring(0, q);
        if (db.isBlank()) throw new IllegalArgumentException("Database name is blank in: " + jdbcUrl);
        return db;
    }

    static String replaceDatabaseName(String jdbcUrl, String newDb) {
        int q = jdbcUrl.indexOf('?');
        String base = q == -1 ? jdbcUrl : jdbcUrl.substring(0, q);
        String query = q == -1 ? "" : jdbcUrl.substring(q);

        int lastSlash = base.lastIndexOf('/');
        if (lastSlash == -1) throw new IllegalArgumentException("Invalid JDBC url: " + jdbcUrl);
        return base.substring(0, lastSlash + 1) + newDb + query;
    }
}


