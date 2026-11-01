package ai.halience.aql.domain;

import java.util.UUID;

/**
 * Stable typed identities (AR-01): every AQL object id carries its type prefix, e.g. {@code evd_3f2a...} for Evidence,
 * so a reference says what it points to without a lookup and survives moving between stores.
 */
public final class Ids {

    private Ids() {
    }

    public static String next(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    /** The type prefix of a typed id, e.g. {@code evd} for {@code evd_3f2a...}; empty if it has none. */
    public static String prefixOf(String id) {
        int underscore = id == null ? -1 : id.indexOf('_');
        return underscore <= 0 ? "" : id.substring(0, underscore);
    }
}
