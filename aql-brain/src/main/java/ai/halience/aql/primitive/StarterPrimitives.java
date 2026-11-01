package ai.halience.aql.primitive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/** Reads the starter primitives shipped with the application ({@code brain/**}/{@code *.md} on the classpath). */
public final class StarterPrimitives {

    private static final String ROOT = "/brain/";

    private StarterPrimitives() {
    }

    /** A starter file: its path relative to {@code brain/} and its Markdown. */
    public record Source(String path, String markdown) {
    }

    public static List<Source> load(String pattern) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
            List<Source> sources = new ArrayList<>();
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                String url = resource.getURL().toString().replace('\\', '/');
                int root = url.lastIndexOf(ROOT);
                if (root < 0) {
                    continue;
                }
                try (InputStream in = resource.getInputStream()) {
                    sources.add(new Source(url.substring(root + ROOT.length()),
                            new String(in.readAllBytes(), StandardCharsets.UTF_8)));
                }
            }
            return sources;
        } catch (IOException e) {
            throw new IllegalStateException("Can't read starter primitives from " + pattern, e);
        }
    }

    /** Parses every source; a source that can't be parsed fails with its path in the message. */
    public static PrimitiveSet parse(List<Source> sources) {
        List<Primitive> primitives = new ArrayList<>();
        for (Source source : sources) {
            try {
                primitives.add(Primitive.parse(source.path(), source.markdown()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(source.path() + ": " + e.getMessage(), e);
            }
        }
        return new PrimitiveSet(primitives);
    }
}
