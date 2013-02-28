package eu.mjdev.commons.codec;

/**
 * Defines common encoding methods for byte array encoders.
 * 
 * @author Apache Software Foundation
 * @version $Id: BinaryEncoder.java 1157192 2011-08-12 17:27:38Z ggregory $
 */
public interface BinaryEncoder extends Encoder {
    
    /**
     * Encodes a byte array and return the encoded data
     * as a byte array.
     * 
     * @param source Data to be encoded
     *
     * @return A byte array containing the encoded data
     * 
     * @throws EncoderException thrown if the Encoder
     *      encounters a failure condition during the
     *      encoding process.
     */
    byte[] encode(byte[] source) throws EncoderException;
}  

