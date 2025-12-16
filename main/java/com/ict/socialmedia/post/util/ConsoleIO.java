package com.ict.socialmedia.post.util;

import java.util.Scanner;

public class ConsoleIO {
    private final Scanner scanner;

    public ConsoleIO() {
        this.scanner = new Scanner(System.in);
    }

    public String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int askInt(String prompt, int fallback) {
        String raw = ask(prompt);
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}


