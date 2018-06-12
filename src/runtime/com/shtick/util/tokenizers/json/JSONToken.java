/**
 * 
 */
package com.shtick.util.tokenizers.json;

import com.shtick.util.tokenizers.StandardToken;

/**
 * @author sean.cox
 *
 */
public class JSONToken extends StandardToken<JSONToken> {
	/**
	 * @param startLine
	 * @param startLinePosition
	 * @param endLine
	 * @param endLinePosition
	 * @param startPosition 
	 * @param endPosition 
	 */
	public JSONToken(int startLine, int startLinePosition, int endLine, int endLinePosition, int startPosition, int endPosition) {
		super(startLine, startLinePosition, endLine, endLinePosition, startPosition, endPosition);
	}
}
