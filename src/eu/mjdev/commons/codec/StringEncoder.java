package eu.mjdev.commons.codec;

/**
 * Defines common encoding methods for String encoders.
 *
 * @author Apache Software Foundation
 * @version $Id: StringEncoder.java 1170351 2011-09-13 21:09:09Z ggregory $
 */
public interface StringEncoder extends Encoder {
    
    /**
     * Encodes a String and returns a String.
     * 
     * @param source the String to encode
     * 
     * @return the encoded String
     * 
     * @throws EncoderException thrown if there is
     *  an error condition during the encoding process.
     */
    String encode(String source) throws EncoderException;
}  

