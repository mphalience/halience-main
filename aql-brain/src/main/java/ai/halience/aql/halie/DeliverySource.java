package ai.halience.aql.halie;

import java.util.List;
import java.util.Map;

import ai.halience.aql.governance.Actor;

/**
 * A read-only delivery connector for Halie (CX-16): requirements documents, the issue tracker, repositories. Phase 1
 * delivery intelligence only reads. Implementations must check the actor's access in the source system and report an
 * unavailable or stale source as such, distinct from "nothing found".
 */
public interface DeliverySource {

    /** e.g. {@code requirements}, {@code issues}, {@code repository}. */
    String kind();

    /** Items matching {@code query}, each with at least {@code ref}, {@code title}, {@code status}, {@code updated}. */
    List<Map<String, Object>> search(String query, Actor actor) throws Exception;
}
