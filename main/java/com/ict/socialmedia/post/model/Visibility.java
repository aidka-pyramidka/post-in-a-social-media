package com.ict.socialmedia.post.model;

public enum Visibility {
    PUBLIC,
    FRIENDS,
    PRIVATE;

    public static Visibility parseOrDefault(String raw, Visibility fallback) {
        if (raw == null) return fallback;
        try {
            return Visibility.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}


