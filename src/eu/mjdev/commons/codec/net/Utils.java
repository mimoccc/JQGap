package eu.mjdev.commons.codec.net;

import eu.mjdev.commons.codec.DecoderException;

/**
 * Utility methods for this package.
 * 
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @version $Id: Utils.java 1157192 2011-08-12 17:27:38Z ggregory $
 * @since 1.4
 */
class Utils {

    /**
     * Returns the numeric value of the character <code>b</code> in radix 16.
     * 
     * @param b
     *            The byte to be converted.
     * @return The numeric value represented by the character in radix 16.
     * 
     * @throws DecoderException
     *             Thrown when the byte is not valid per {@link Character#digit(char,int)}
     */
    static int digit16(byte b) throws DecoderException {
        int i = Character.digit((char) b, 16);
        if (i == -1) {
            throw new DecoderException("Invalid URL encoding: not a valid digit (radix " + URLCodec.RADIX + "): " + b);
        }
        return i;
    }

}
