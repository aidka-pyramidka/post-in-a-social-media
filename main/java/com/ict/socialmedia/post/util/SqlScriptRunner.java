package com.ict.socialmedia.post.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class SqlScriptRunner {
    private SqlScriptRunner() {
    }

    public static void runClasspathResource(Connection connection, String resourcePath) {
        String sql = readClasspathText(resourcePath);
        runSqlStatements(connection, splitStatements(sql));
    }

    private static String readClasspathText(String resourcePath) {
        try (InputStream in = SqlScriptRunner.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("SQL resource not found: " + resourcePath);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read SQL resource: " + resourcePath, e);
        }
    }

    private static List<String> splitStatements(String sql) {
        String[] parts = sql.split(";");
        List<String> statements = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim();
            if (s.isEmpty()) continue;
            statements.add(s);
        }
        return statements;
    }

    private static void runSqlStatements(Connection connection, List<String> statements) {
        try (Statement st = connection.createStatement()) {
            for (String s : statements) {
                st.execute(s);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SQL script", e);
        }
    }
}


