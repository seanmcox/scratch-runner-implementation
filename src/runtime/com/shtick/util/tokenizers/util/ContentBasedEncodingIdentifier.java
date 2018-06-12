/**
 * 
 */
package com.shtick.util.tokenizers.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.IllegalCharsetNameException;

/**
 * An interface for a class which identifies an encoding based on the content of the file.
 * This is used by TokenizerInput to identify encoding based on content after a possible watermark.
 * 
 * @author sean.cox
 *
 */
public interface ContentBasedEncodingIdentifier {
	/**
	 * 
	 * @param in The InputStream for which to identify the encoding. If the content is to be used
	 *           separately, then a rewindable InputStream should be used here.
	 * @param bomIdentification The character set identified by the byte order mark, if any,
	 *                          or null if no byte order mark identification is made.
	 *                          When set, this should be the name of a character set.
	 * @return The name of the encoding, or null, if none could be identified.
	 * @throws IOException 
	 * @throws InvalidCharsetIdentificationException If the textual character set identification is internally self-contradictory. 
	 * @throws IllegalCharsetNameException If bomIdentification is not a valid character set name.
	 *                                     May not be thrown for a given call if the bomIdentification is not needed to help resolve a character set.
	 */
	String identifyEncoding(InputStream in, String bomIdentification) throws IOException, InvalidCharsetIdentificationException, IllegalCharsetNameException;
}
