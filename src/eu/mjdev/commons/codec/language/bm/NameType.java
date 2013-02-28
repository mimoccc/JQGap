package eu.mjdev.commons.codec.language.bm;

/**
 * Supported types of names. Unless you are matching particular family names, use {@link #GENERIC}. The
 * <code>GENERIC</code> NameType should work reasonably well for non-name words. The other encodings are specifically
 * tuned to family names, and may not work well at all for general text.
 * 
 * @author Apache Software Foundation
 * @since 1.6
 */
public enum NameType {

    /** Ashkenazi family names */
    ASHKENAZI("ash"),

    /** Generic names and words */
    GENERIC("gen"),

    /** Sephardic family names */
    SEPHARDIC("sep");

    private final String name;

    NameType(String name) {
        this.name = name;
    }

    /**
     * Gets the short version of the name type.
     * 
     * @return the NameType short string
     */
    public String getName() {
        return this.name;
    }
}
