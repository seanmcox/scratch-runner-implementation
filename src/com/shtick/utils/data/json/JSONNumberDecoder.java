/**
 * 
 */
package com.shtick.utils.data.json;

import com.shtick.util.tokenizers.json.NumberToken;

/**
 * @author sean.cox
 *
 */
public interface JSONNumberDecoder {
	/**
	 * 
	 * @param token
	 * @return An object representing the decoded number.
	 */
	public Object decodeNumber(NumberToken token);
}
