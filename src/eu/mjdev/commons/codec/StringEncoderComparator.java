package eu.mjdev.commons.codec;

import java.util.Comparator;

/**
 * Compares Strings using a {@link StringEncoder}. This comparator is used to sort Strings by an encoding scheme such as
 * Soundex, Metaphone, etc. This class can come in handy if one need to sort Strings by an encoded form of a name such
 * as Soundex.
 * 
 * @author Apache Software Foundation
 * @version $Id: StringEncoderComparator.java 1201520 2011-11-13 21:36:18Z ggregory $
 */
public class StringEncoderComparator implements Comparator<Object> {

    /**
     * Internal encoder instance.
     */
    private final StringEncoder stringEncoder;

    /**
     * Constructs a new instance.
     * 
     * @deprecated Creating an instance without a {@link StringEncoder} leads to a {@link NullPointerException}. Will be
     *             removed in 2.0.
     */
    public StringEncoderComparator() {
        this.stringEncoder = null; // Trying to use this will cause things to break
    }

    /**
     * Constructs a new instance with the given algorithm.
     * 
     * @param stringEncoder
     *            the StringEncoder used for comparisons.
     */
    public StringEncoderComparator(StringEncoder stringEncoder) {
        this.stringEncoder = stringEncoder;
    }

    /**
     * Compares two strings based not on the strings themselves, but on an encoding of the two strings using the
     * StringEncoder this Comparator was created with.
     * 
     * If an {@link EncoderException} is encountered, return <code>0</code>.
     * 
     * @param o1
     *            the object to compare
     * @param o2
     *            the object to compare to
     * @return the Comparable.compareTo() return code or 0 if an encoding error was caught.
     * @see Comparable
     */
    @SuppressWarnings("unchecked")
	public int compare(Object o1, Object o2) {

        int compareCode = 0;

        try {
            Comparable<Comparable<?>> s1 = (Comparable<Comparable<?>>) this.stringEncoder.encode(o1);
            Comparable<?> s2 = (Comparable<?>) this.stringEncoder.encode(o2);
            compareCode = s1.compareTo(s2);
        } catch (EncoderException ee) {
            compareCode = 0;
        }
        return compareCode;
    }

}
