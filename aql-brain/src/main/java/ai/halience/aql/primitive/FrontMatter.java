package ai.halience.aql.primitive;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A primitive's Markdown split into its front matter ({@code key: value} lines between two {@code ---} lines) and its
 * body. Keys are lower-cased; values are trimmed; {@code #} starts a comment. No YAML: lists are comma-separated.
 */
public record FrontMatter(Map<String, String> fields, String body) {

    public FrontMatter {
        fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        body = body == null ? "" : body;
    }

    public static FrontMatter parse(String markdown) {
        String text = markdown == null ? "" : markdown.replace("\r\n", "\n");
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        String[] lines = text.split("\n", -1);
        if (lines.length == 0 || !lines[0].strip().equals("---")) {
            return new FrontMatter(Map.of(), text.strip());
        }
        Map<String, String> fields = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.strip().equals("---")) {
                String body = String.join("\n", Arrays.copyOfRange(lines, i + 1, lines.length)).strip();
                return new FrontMatter(fields, body);
            }
            String content = stripComment(line).strip();
            if (content.isEmpty()) {
                continue;
            }
            int colon = content.indexOf(':');
            if (colon <= 0) {
                throw new IllegalArgumentException("Front matter line " + (i + 1) + " isn't 'key: value': " + line);
            }
            String key = content.substring(0, colon).strip().toLowerCase(Locale.ROOT);
            String value = content.substring(colon + 1).strip();
            if (fields.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Front matter key '" + key + "' appears twice");
            }
        }
        throw new IllegalArgumentException("Front matter starting with '---' is never closed by another '---' line");
    }

    /** A comment starts at a {@code #} that begins the line or follows whitespace, so values like {@code C#} survive. */
    private static String stripComment(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '#' && (i == 0 || Character.isWhitespace(line.charAt(i - 1)))) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    public String get(String key) {
        return fields.get(key);
    }

    public String getOrDefault(String key, String fallback) {
        String value = fields.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    /** A comma-separated field as a list of trimmed, non-empty values; empty when absent. */
    public List<String> list(String key) {
        return splitList(fields.get(key));
    }

    public static List<String> splitList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList();
    }
}
