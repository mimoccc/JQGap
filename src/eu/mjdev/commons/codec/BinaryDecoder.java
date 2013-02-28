package eu.mjdev.commons.codec;

/**
 * Defines common decoding methods for byte array decoders.
 *
 * @author Apache Software Foundation
 * @version $Id: BinaryDecoder.java 1157192 2011-08-12 17:27:38Z ggregory $
 */
public interface BinaryDecoder extends Decoder {

    /**
     * Decodes a byte array and returns the results as a byte array. 
     *
     * @param source A byte array which has been encoded with the
     *      appropriate encoder
     * 
     * @return a byte array that contains decoded content
     * 
     * @throws DecoderException A decoder exception is thrown
     *          if a Decoder encounters a failure condition during
     *          the decode process.
     */
    byte[] decode(byte[] source) throws DecoderException;
}  

