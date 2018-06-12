/**
 * 
 */
package com.shtick.util.tokenizers.json;

/**
 * @author sean.cox
 *
 */
public class DoubleQuoteToken extends JSONToken {
	/**
	 * The constant value that characterizes this token.
	 * This is the same as the value returned by toString()
	 */
	public static final String VALUE="\"";

	/**
	 * @param line
	 * @param linePosition 
	 * @param position
	 */
	public DoubleQuoteToken(int line, int linePosition, int position) {
		super(line, linePosition, line, linePosition+VALUE.length(), position, position+VALUE.length());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return VALUE;
	}

}
