package eu.mjdev.commons.codec.language.bm;

/**
 * Types of rule.
 * 
 * @author Apache Software Foundation
 * @since 1.6
 */
public enum RuleType {

    /** Approximate rules, which will lead to the largest number of phonetic interpretations. */
    APPROX("approx"),
    /** Exact rules, which will lead to a minimum number of phonetic interpretations. */
    EXACT("exact"),
    /** For internal use only. Please use {@link #APPROX} or {@link #EXACT}. */
    RULES("rules");

    private final String name;

    RuleType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
