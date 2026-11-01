package ai.halience.aql.governance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ai.halience.aql.domain.Json;

/**
 * A SHA-256 hash of an action's canonical payload. Map keys are sorted recursively, so the same content always hashes
 * the same and any material change produces a different hash, which invalidates an approval (LP-15 reapproval).
 */
public final class PayloadHasher {

    private PayloadHasher() {
    }

    public static String hash(String actionType, String target, Map<String, Object> payload) {
        Map<String, Object> envelope = new TreeMap<>();
        envelope.put("actionType", actionType);
        envelope.put("target", target);
        envelope.put("payload", canonical(payload));
        return sha256(Json.write(envelope));
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object canonical(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            ((Map<Object, Object>) map).forEach((k, v) -> sorted.put(String.valueOf(k), canonical(v)));
            return sorted;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(PayloadHasher::canonical).collect(Collectors.toList());
        }
        return value;
    }
}
