package ai.halience.aql.primitive;

import java.util.Arrays;
import java.util.Optional;

/**
 * The five primitive types Halie is made of and can edit: behaviours (how to do a kind of work, including shared
 * skills), knowledge (what is true), journeys (dependent milestones toward one goal), routines (regular checks that
 * call the other four) and tools (actuators on an LLM call). Each lives in its own folder under {@code brain/}.
 */
public enum PrimitiveType {
    BEHAVIOUR("behaviours"),
    KNOWLEDGE("knowledge"),
    JOURNEY("journeys"),
    ROUTINE("routines"),
    TOOL("tools");

    /** Behaviours in this sub-folder are skills: shared conduct included into agents, never run on their own. */
    public static final String SKILLS_FOLDER = "skills";

    private final String folder;

    PrimitiveType(String folder) {
        this.folder = folder;
    }

    public String folder() {
        return folder;
    }

    public static Optional<PrimitiveType> fromFolder(String folder) {
        return Arrays.stream(values()).filter(t -> t.folder.equals(folder)).findFirst();
    }
}
