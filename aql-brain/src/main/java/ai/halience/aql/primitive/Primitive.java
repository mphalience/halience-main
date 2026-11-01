package ai.halience.aql.primitive;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * One parsed primitive. Its {@code key} is its path under {@code brain/} without {@code .md}, e.g.
 * {@code behaviours/halie/halie}; the key is permanent because other primitives and code refer to it.
 *
 * @param folder the sub-folder within the type's folder ({@code halie} for {@code behaviours/halie/halie}); empty when
 *               the file sits directly in the type folder
 */
public record Primitive(PrimitiveType type, String key, String folder, String name, String description, int phase,
                        FrontMatter frontMatter, String markdown) {

    public static final Pattern NAME = Pattern.compile("^[a-z0-9_]{1,64}$");

    public Primitive {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(key, "key");
        folder = folder == null ? "" : folder;
    }

    /**
     * Parses {@code markdown} found at {@code path} (relative to {@code brain/}, forward slashes, e.g.
     * {@code behaviours/halie/halie.md}).
     */
    public static Primitive parse(String path, String markdown) {
        String clean = path.replace('\\', '/');
        if (clean.startsWith("/")) {
            clean = clean.substring(1);
        }
        if (!clean.endsWith(".md")) {
            throw new IllegalArgumentException("A primitive must be a .md file: " + path);
        }
        String key = clean.substring(0, clean.length() - 3);
        String[] parts = key.split("/");
        PrimitiveType type = PrimitiveType.fromFolder(parts[0])
                .orElseThrow(() -> new IllegalArgumentException("Unknown primitive folder '" + parts[0] + "' in " + path));
        String folder = parts.length > 2 ? String.join("/", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1)) : "";
        String fileName = parts[parts.length - 1];
        FrontMatter frontMatter = FrontMatter.parse(markdown);
        String name = frontMatter.getOrDefault("name", fileName);
        int phase = parsePhase(frontMatter.get("phase"));
        return new Primitive(type, key, folder, name, frontMatter.getOrDefault("description", ""), phase, frontMatter,
                markdown);
    }

    static int parsePhase(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            int phase = Integer.parseInt(value.strip());
            if (phase < 1 || phase > 3) {
                throw new IllegalArgumentException("phase must be 1, 2 or 3; was " + value);
            }
            return phase;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("phase must be 1, 2 or 3; was " + value);
        }
    }

    /** The file name without {@code .md}; must equal {@link #name()}. */
    public String fileName() {
        return key.substring(key.lastIndexOf('/') + 1);
    }

    public String body() {
        return frontMatter.body();
    }

    public String field(String key) {
        return frontMatter.get(key);
    }

    public List<String> list(String key) {
        return frontMatter.list(key);
    }

    /** Whether this is a skill (a behaviour in {@code behaviours/skills/}). */
    public boolean isSkill() {
        return type == PrimitiveType.BEHAVIOUR && PrimitiveType.SKILLS_FOLDER.equals(folder);
    }

    /** Whether this is a runnable agent behaviour (a behaviour that is not a skill). */
    public boolean isAgent() {
        return type == PrimitiveType.BEHAVIOUR && !isSkill();
    }
}
