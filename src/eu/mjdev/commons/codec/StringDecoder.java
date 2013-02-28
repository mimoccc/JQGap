package eu.mjdev.commons.codec;

/**
 * Defines common decoding methods for String decoders.
 *
 * @author Apache Software Foundation
 * @version $Id: StringDecoder.java 1157192 2011-08-12 17:27:38Z ggregory $
 */
public interface StringDecoder extends Decoder {
    
    /**
     * Decodes a String and returns a String.
     * 
     * @param source the String to decode
     * 
     * @return the encoded String
     * 
     * @throws DecoderException thrown if there is
     *  an error condition during the Encoding process.
     */
    String decode(String source) throws DecoderException;
}  

